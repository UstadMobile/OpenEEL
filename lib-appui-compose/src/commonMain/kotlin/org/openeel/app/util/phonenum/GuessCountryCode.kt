package world.respect.app.util.phonenum

import androidx.compose.runtime.Composable
import world.respect.shared.domain.phonenumber.IPhoneNumberUtil

@Composable
expect fun guessInitialPhoneCountryCode(
    phoneUtil: IPhoneNumberUtil
) : Int?
