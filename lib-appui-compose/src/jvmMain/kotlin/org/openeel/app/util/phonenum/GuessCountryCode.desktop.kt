package world.respect.app.util.phonenum

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import world.respect.shared.domain.phonenumber.IPhoneNumberUtil
import java.util.Locale

@Composable
actual fun guessInitialPhoneCountryCode(
    phoneUtil: IPhoneNumberUtil
) : Int? {
    return remember {
        try {
            phoneUtil.getCountryCodeForRegion(Locale.getDefault().country)
        }catch(e: Throwable) {
            null
        }
    }
}
