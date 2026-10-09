package world.respect.shared.domain.getwarnings

import android.os.Build
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.android6_warning
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText

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