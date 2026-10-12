package org.openeel.lib.xapi.ext

import org.openeel.lib.xapi.exceptions.XapiException

fun Throwable.xapiHttpStatusCodeOrNull() : Int? {
    return (this as? XapiException)?.httpStatusCode
}