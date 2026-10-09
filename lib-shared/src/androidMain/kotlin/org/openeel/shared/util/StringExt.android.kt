package world.respect.shared.util

import android.util.Base64

/**
 * To ensure consistency between JVM and Android, Base64 encoding
 * **must** be done with NO_WRAP
 */
actual fun String.base64StringToByteArray()= Base64.decode(this, Base64.NO_WRAP)