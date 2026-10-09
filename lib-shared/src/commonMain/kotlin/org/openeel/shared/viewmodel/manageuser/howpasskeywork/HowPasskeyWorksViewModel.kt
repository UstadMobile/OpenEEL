package world.respect.shared.viewmodel.manageuser.howpasskeywork

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.update
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.how_passkey_works
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel


class HowPasskeyWorksViewModel(
    savedStateHandle: SavedStateHandle,
) : RespectViewModel(savedStateHandle) {
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
