package org.openeel.shared.viewmodel.report

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.update
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.report
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel

class ReportViewModel(
    savedStateHandle: SavedStateHandle
) : OpenEelViewModel(savedStateHandle) {

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.report.asUiText(),
            )
        }
    }

}
