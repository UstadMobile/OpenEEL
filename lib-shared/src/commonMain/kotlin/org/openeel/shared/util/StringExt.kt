package world.respect.shared.util

fun String.firstNonWhiteSpaceChar(): Char? {
    val index = indexOfFirst { !it.isWhitespace() }
    return if(index != -1)
        this[index]
    else
        null
}

fun String.initial(): String {
    return firstNonWhiteSpaceChar()?.uppercase() ?: ""
}

/**
 * To ensure consistency between JVM and Android, Base64 encoding
 * **must** be done with NO_WRAP
 */
expect fun String.base64StringToByteArray(): ByteArray