package world.respect.shared.domain.phonenumber

import io.michaelrocks.libphonenumber.android.Phonenumber


class PhoneNumberAndroid(
    internal val phoneNumber: Phonenumber.PhoneNumber
) : IPhoneNumber {
    override val countryCode: Int
        get() = phoneNumber.countryCode
    override val nationalNumber: Long
        get() = phoneNumber.nationalNumber
}