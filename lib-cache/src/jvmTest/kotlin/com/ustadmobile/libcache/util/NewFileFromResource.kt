package com.ustadmobile.libcache.util

import org.junit.rules.TemporaryFolder
import org.openeel.libutil.util.time.systemTimeInMillis
import java.io.File
import java.io.FileOutputStream


/**
 * Convenience function to create a temporary file and copy the content from a resource.
 * @param lastModifiedTime The last modified time to set on the file. It is not possible
 *        to get the real last modified time using the resources API. Setting this avoids
 *        confusing other logic that uses the last-modified time.
 */
fun TemporaryFolder.newFileFromResource(
    clazz: Class<*>,
    resourcePath: String,
    fileName: String? = null,
    lastModifiedTime: Long = systemTimeInMillis() - 60_000L,
): File {
    val file = if(fileName != null) newFile(fileName) else newFile()
    clazz.getResourceAsStream(resourcePath)!!.use { resourceIn ->
        FileOutputStream(file).use { fileOut ->
            resourceIn.copyTo(fileOut)
            fileOut.flush()
        }
    }
    return file.also {
        it.setLastModified(lastModifiedTime)
    }
}