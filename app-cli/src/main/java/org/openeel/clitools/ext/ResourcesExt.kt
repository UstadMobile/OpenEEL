package world.respect.clitools.ext

import java.io.File
import java.io.FileOutputStream

fun Class<*>.copyResourceToFile(
    resourceName: String,
    destFile: File,
) {
    getResourceAsStream(resourceName).use { inputStream ->
        FileOutputStream(destFile).use { outputStream ->
            inputStream.copyTo(outputStream)
            outputStream.flush()
        }
    }
}
