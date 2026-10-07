package org.openeel.shared.domain.getwarnings

import android.os.Build
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.android6_warning
import org.openeel.shared.resources.UiText
import org.openeel.shared.util.ext.asUiText

class GetWarningsUseCaseAndroid(): GetWarningsUseCase {

    override suspend fun invoke(): UiText? {
        //See https://github.com/UstadMobile/Respect/issues/72
        return if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            Res.string.android6_warning.asUiText()
        }else {
            null
        }
    }
}