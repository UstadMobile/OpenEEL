package world.respect.testutil

import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException

fun TemporaryFolder.copyResourcesToTempDir(
    resourceBasePath: String,
    resourceNames: List<String>,
): File {
    val tempDir = this.newFolder()
    for(resourceName in resourceNames) {
        val destFile = File(tempDir, resourceName)
        destFile.parentFile?.takeIf { !it.exists() }?.mkdirs()
        val resourcePath = "$resourceBasePath/$resourceName"
        val resourceStream = this::class.java.getResourceAsStream(resourcePath)
            ?: throw IOException("copyResourcesToTempDir: Could not load resource: $resourcePath")
        resourceStream.use { resourceIn ->
            destFile.outputStream().use { fileOut ->
                resourceIn.copyTo(fileOut)
                fileOut.flush()
            }
        }
    }

    return tempDir
}
