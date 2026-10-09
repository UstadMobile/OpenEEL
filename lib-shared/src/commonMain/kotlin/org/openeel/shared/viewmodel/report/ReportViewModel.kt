package world.respect.shared.viewmodel.report

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.update
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.report
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

class ReportViewModel(
    savedStateHandle: SavedStateHandle
) : RespectViewModel(savedStateHandle) {

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.report.asUiText(),
            )
        }
    }

}
