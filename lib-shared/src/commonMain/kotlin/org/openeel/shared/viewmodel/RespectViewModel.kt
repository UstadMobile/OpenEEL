package world.respect.shared.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.koin.mp.KoinPlatform.getKoin
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResult
import world.respect.shared.navigation.NavResultReturner
import world.respect.libutil.util.time.systemTimeInMillis
import world.respect.shared.resources.UiText
import world.respect.shared.util.exception.getUiTextOrGeneric
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.viewmodel.app.appstate.LoadingUiState

abstract class RespectViewModel(
    protected val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    protected val _appUiState = MutableStateFlow(AppUiState())

    val appUiState: StateFlow<AppUiState> = _appUiState.asStateFlow()

    protected val _navCommandFlow = MutableSharedFlow<NavCommand>(
        replay = 1,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val navCommandFlow: Flow<NavCommand> = _navCommandFlow.asSharedFlow()

    /**
     * Shorthand to make it easier to update the loading state
     */
    @Suppress("unused")//reserved for future use
    protected var loadingState: LoadingUiState
        get() = _appUiState.value.loadingState
        set(value) {
            _appUiState.update {
                it.copy(loadingState = value)
            }
        }


    private var lastNavResultTimestampCollected: Long = savedStateHandle.get<String>(
        KEY_LAST_COLLECTED_TS
    )?.toLong() ?: 0L
        set(value) {
            field = value
            savedStateHandle[KEY_LAST_COLLECTED_TS] = value.toString()
        }



    /**
     * Used to 'return' a result to a previous screen in the stack.
     *
     * E.g. the user is on EditScreen, then navigates to EditOptionScreen to change options, and
     * the result of EditOptionScreen needs to be used in EditScreen.
     *
     * The result from sendResultAndPop should be collected using filteredResultFlowForKey
     *
     * @param destKey the argument key name (used to avoid conflicts)
     * @param result the result being returned
     */
    protected fun sendResultAndPop(
        destKey: String,
        result: Any?,
    ) {
        val navResultReturner: NavResultReturner = getKoin().get()
        navResultReturner.sendResult(
            result = NavResult(
                key = destKey,
                timestamp = systemTimeInMillis(),
                result = result,
            )
        )
        _navCommandFlow.tryEmit(NavCommand.PopUp())
    }

    /**
     * Shorthand to observe results. Avoids two edge cases:
     *
     * 1. "Replay" - when the ViewModel is recreated, if no other result has been returned in the
     *    meantime, the last result would be collected again. The flow of NavResultReturner always
     *    replays the most recent result returned (required to allow a collector which starts after
     *    the result was sent to collect it).
     *
     *    This is avoided by tracking the timestamp of the last item collected.
     *
     * 2. Replay from previous viewmodel: when the user goes from screen A to screen B, then C,
     *    returns a result to screen A, and then navigates forward to screen B again with new arguments.
     *    The new instance of screen B does not remember receiving any results, so the result from
     *    the old instance of screen C looks new.
     *
     *    This is avoided by setting the alstNavResultTimestampCollected to the first start time
     *    on init.
     *
     */
    fun NavResultReturner.filteredResultFlowForKey(
        key: String,
    ) : Flow<NavResult> {
        return resultFlowForKey(key).filter {
            val isNew = it.timestamp > lastNavResultTimestampCollected
            if(isNew)
                lastNavResultTimestampCollected = it.timestamp

            isNew
        }
    }

    /**
     * Load an entity for use in edit screens. Strategy:
     * a) Check the SavedState - this is where we will find the entity if the user was editing it but
     *    did not save it yet (can happen when app is destroyed or when user is moving between
     *    screens).
     * b) Try loading locally (using DataLoadParams) so we can (immediately) show the entity to the
     *    user.
     * c) Check/refresh the entity from the remote server.
     *
     * @param initialStateKey if not null, then save data loaded from the database/remotely to the
     *        specified key in the SavedStateHandle. This can be used by a ViewModel to detect if
     *        data has been changed since loading.
     */
    suspend fun <T: Any> loadEntity(
        json: Json,
        serializer: KSerializer<T>,
        savedStateKey: String = DEFAULT_SAVED_STATE_KEY,
        initialStateKey: String? = null,
        loadFn: suspend (DataLoadParams) -> DataLoadState<T>,
        uiUpdateFn: (DataLoadState<T>) -> Unit,
    ): DataLoadState<T> {
        fun setInitialStateIfNeeded(loadState: DataLoadState<T>) {
            val dataLoaded = loadState.dataOrNull()
            if (dataLoaded != null && initialStateKey != null) {
                savedStateHandle[initialStateKey] = json.encodeToString(
                    serializer, dataLoaded
                )
            }
        }

        val entityInSavedState = savedStateHandle.get<String>(savedStateKey)?.let {
            json.decodeFromString(serializer, it)
        }

        if(entityInSavedState != null) {
            val dataState = DataReadyState(entityInSavedState)
            uiUpdateFn(dataState)
            return dataState
        }

        //try and get a local version first if available
        val localEntity = try {
            loadFn(DataLoadParams(onlyIfCached = true)).also {
                uiUpdateFn(it)
                setInitialStateIfNeeded(it)
            }
        }catch(_: Throwable) {
            //Log it
            null
        }

        val remoteEntity = try {
            loadFn(DataLoadParams()).also {
                uiUpdateFn(it)
                setInitialStateIfNeeded(it)
            }
        }catch(e: Throwable) {
            //Log it
            null
        }

        return remoteEntity ?: localEntity ?: NoDataLoadedState.notFound()
    }


    init {
        if(lastNavResultTimestampCollected == 0L)
            lastNavResultTimestampCollected = systemTimeInMillis()
    }

    /**
     * Run the given block using viewModelScope.launch. When the block is running set app ui state
     * to loading.
     *
     * @param runIfAlreadyLoading by default, if loading is already in progress, the block will not
     *        be run.
     * @param onShowError optional function that will show an error message to the user e.g. by
     *        updating the UiState. If this is non-null, the exception will be considered handled
     *        and WILL NOT be rethrown.
     * @param block suspended function to run
     */
    fun launchWithLoadingIndicator(
        runIfAlreadyLoading: Boolean = false,
        onShowError: ((UiText) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit,
    ) {
        if(!runIfAlreadyLoading && loadingState == LoadingUiState.INDETERMINATE) {
            Napier.d("launchWithLoadingIndicator: already loading")
            return
        }

        viewModelScope.launch {
            loadingState = LoadingUiState.INDETERMINATE
            try {
                block()
            }catch(e: Throwable) {
                if(onShowError != null) {
                    Napier.w(
                        message = "Error in launchWithLoadingIndicator - displaying to user",
                        throwable = e
                    )
                    onShowError(e.getUiTextOrGeneric())
                }else {
                    throw e
                }
            }finally {
                loadingState = LoadingUiState.NOT_LOADING
            }
        }
    }

    companion object {

        const val DEFAULT_SAVED_STATE_KEY = "entity"

        const val KEY_LAST_COLLECTED_TS = "collectedTs"

        const val KEY_INITIAL_STATE = "initstate"

    }
}