package world.respect.shared.util

import java.util.Base64

/**
 * To ensure consistency between JVM and Android, Base64 encoding
 * **must** be done with NO_WRAP
 */
actual fun String.base64StringToByteArray() = Base64.getDecoder().decode(this)