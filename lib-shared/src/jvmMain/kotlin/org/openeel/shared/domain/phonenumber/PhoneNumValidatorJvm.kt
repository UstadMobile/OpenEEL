package com.ustadmobile.core.domain.phonenumber

import org.openeel.shared.domain.phonenumber.IPhoneNumberUtil
import org.openeel.shared.domain.phonenumber.PhoneNumValidatorUseCase


class PhoneNumValidatorJvm(
    private val iPhoneNumberUtil: IPhoneNumberUtil
): PhoneNumValidatorUseCase {
    override fun isValid(phoneNumber: String): Boolean {
        return try {
            iPhoneNumberUtil.isValidNumber(iPhoneNumberUtil.parse(phoneNumber, "US"))
        }catch(e: Throwable) {
            false
        }
    }

}