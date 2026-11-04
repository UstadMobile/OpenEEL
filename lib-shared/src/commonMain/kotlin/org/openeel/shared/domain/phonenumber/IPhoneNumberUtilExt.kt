package com.ustadmobile.core.domain.phonenumber

import org.openeel.shared.domain.phonenumber.IPhoneNumberUtil

fun IPhoneNumberUtil.formatInternationalOrNull(number: String): String? {
    return try {
        formatInternational(parse(number, "US"))
    }catch(e: Throwable) {
        null
    }
}