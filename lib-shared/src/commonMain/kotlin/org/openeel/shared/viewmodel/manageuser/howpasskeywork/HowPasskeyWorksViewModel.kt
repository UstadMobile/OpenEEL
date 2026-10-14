package org.openeel.shared.viewmodel.manageuser.howpasskeywork

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.update
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.how_passkey_works
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel


class HowPasskeyWorksViewModel(
    savedStateHandle: SavedStateHandle,
) : OpenEelViewModel(savedStateHandle) {
    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.how_passkey_works.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false,
            )
        }
    }
}
