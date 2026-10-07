package org.openeel.shared.viewmodel.report

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.update
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.report
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.RespectViewModel

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
