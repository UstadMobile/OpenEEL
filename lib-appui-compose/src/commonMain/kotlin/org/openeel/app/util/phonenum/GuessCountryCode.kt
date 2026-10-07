package org.openeel.app.util.phonenum

import androidx.compose.runtime.Composable
import org.openeel.shared.domain.phonenumber.IPhoneNumberUtil

@Composable
expect fun guessInitialPhoneCountryCode(
    phoneUtil: IPhoneNumberUtil
) : Int?
