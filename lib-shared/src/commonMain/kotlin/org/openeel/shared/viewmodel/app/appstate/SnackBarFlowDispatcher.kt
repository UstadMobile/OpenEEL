package world.respect.shared.viewmodel.app.appstate

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SnackBarFlowDispatcher: SnackBarDispatcher {

    private val _snackFlow = MutableSharedFlow<Snack>(
        replay = 1,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val snackFlow = _snackFlow.asSharedFlow()

    override fun showSnackBar(snack: Snack) {
        _snackFlow.tryEmit(snack)
    }
}