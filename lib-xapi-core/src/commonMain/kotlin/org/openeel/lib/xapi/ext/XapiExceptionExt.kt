package world.respect.lib.xapi.ext

import world.respect.lib.xapi.exceptions.XapiException

fun Throwable.xapiHttpStatusCodeOrNull() : Int? {
    return (this as? XapiException)?.httpStatusCode
}