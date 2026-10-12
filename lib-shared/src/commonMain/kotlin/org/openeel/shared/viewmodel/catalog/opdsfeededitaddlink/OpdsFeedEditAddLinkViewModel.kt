package org.openeel.shared.viewmodel.catalog.opdsfeededitaddlink

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.openeel.lib.opds.model.LangMapStringValue
import org.openeel.lib.opds.model.Publication
import org.openeel.lib.opds.model.ReadiumLink
import org.openeel.lib.opds.model.ReadiumMetadata
import org.openeel.shared.domain.externallink.ExtractWebPageMetadataUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.could_not_load_link
import org.openeel.shared.generated.resources.done
import org.openeel.shared.generated.resources.external_link
import org.openeel.shared.generated.resources.required_field
import org.openeel.shared.navigation.ExternalLinkEdit
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.NavResult
import org.openeel.shared.navigation.NavResultReturner
import org.openeel.shared.navigation.RouteResultDest
import org.openeel.shared.resources.UiText
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel
import org.openeel.shared.viewmodel.app.appstate.ActionBarButtonUiState
import org.openeel.shared.viewmodel.catalog.PublicationsSelection

data class OpdsFeedEditAddLinkUiState(
    val url: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val urlError: UiText? = null,
    val titleError: UiText? = null,
    val step: Step = Step.URL,
    val isLoading: Boolean = false,
) {
    enum class Step {
        URL,
        METADATA
    }
}

class OpdsFeedEditAddLinkViewModel(
    savedStateHandle: SavedStateHandle,
    private val resultReturner: NavResultReturner,
    private val extractWebPageMetadataUseCase: ExtractWebPageMetadataUseCase,
) : OpenEelViewModel(savedStateHandle) {

    private val route: ExternalLinkEdit = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(OpdsFeedEditAddLinkUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.external_link.asUiText(),
                userAccountIconVisible = false,
                hideBottomNavigation = true,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = false,
                )
            )
        }
    }

    fun onUrlChanged(url: String) {
        _uiState.update {
            it.copy(
                url = url,
                urlError = null
            )
        }
    }

    /**
     * Loads the metadata for the URL that the user has entered so that the title, description, and
     * thumbnail can be shown and edited before the link is added.
     */
    fun onClickNext() {
        val url = _uiState.value.url.trim()

        if (url.isBlank()) {
            _uiState.update {
                it.copy(urlError = Res.string.required_field.asUiText())
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    url = url,
                    isLoading = true
                )
            }
            try {
                val metadata = extractWebPageMetadataUseCase(url)
                _uiState.update {
                    it.copy(
                        step = OpdsFeedEditAddLinkUiState.Step.METADATA,
                        title = metadata.title ?: "",
                        description = metadata.description ?: "",
                        imageUrl = metadata.imageUrl,
                        isLoading = false,
                    )
                }

                _appUiState.update { prev ->
                    prev.copy(
                        actionBarButtonState = ActionBarButtonUiState(
                            visible = true,
                            text = Res.string.done.asUiText(),
                            onClick = ::onClickDone,
                        )
                    )
                }
            } catch (e: Exception) {
                Napier.w("Could not extract metadata for $url", e)

                _uiState.update {
                    it.copy(
                        urlError = Res.string.could_not_load_link.asUiText(),
                        isLoading = false,
                    )
                }
            }
        }
    }
    fun onTitleChanged(title: String) {
        _uiState.update {
            it.copy(
                title = title,
                titleError = null
            )
        }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update {
            it.copy(description = description)
        }
    }

    fun onClickDone() {
        val state = _uiState.value

        if (state.title.isBlank()) {
            _uiState.update {
                it.copy(titleError = Res.string.required_field.asUiText())
            }
            return
        }
        val publication = Publication(
            metadata = ReadiumMetadata(
                title = LangMapStringValue(state.title.trim()),
                description = state.description.trim().takeIf { it.isNotBlank() },
            ),
            links = listOf(
                ReadiumLink(
                    href = state.url,
                    rel = listOf(REL_SELF),
                    type = TYPE_HTML,
                )
            ),
            images = state.imageUrl?.let {
                listOf(ReadiumLink(href = it, type = TYPE_IMAGE))
            }
        )

        val resultDest = route.resultDest
            ?: throw IllegalStateException("resultDest is null")

        resultReturner.sendResult(
            NavResult(
                key = resultDest.resultKey,
                result = PublicationsSelection(
                    url = Url(state.url),
                    selectedPublications = listOf(publication),
                ),
            )
        )

        _navCommandFlow.tryEmit(
            NavCommand.PopToRoute(
                destination = (resultDest as RouteResultDest).resultPopUpTo,
                inclusive = false,
            )
        )
    }
    companion object {
        private const val REL_SELF = "self"
        private const val TYPE_HTML = "text/html"
        private const val TYPE_IMAGE = "image/*"
    }
}