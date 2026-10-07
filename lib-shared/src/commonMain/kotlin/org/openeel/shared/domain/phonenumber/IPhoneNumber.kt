package org.openeel.shared.domain.phonenumber


interface IPhoneNumber {
    val countryCode: Int
    val nationalNumber: Long
}