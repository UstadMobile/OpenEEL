package org.openeel.shared.domain.phonenumber

interface PhoneNumValidatorUseCase {

    fun isValid(phoneNumber: String): Boolean

}