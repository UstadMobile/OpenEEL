package world.respect.shared.util.ext

import java.io.File


/**
 * Determine if the given file is a child of another directory
 */
fun File.isChildOf(parent: File): Boolean {
    val absoluteFile = this.absoluteFile
    val absoluteParent = parent.absoluteFile

    var currentFile: File? = absoluteFile
    while(currentFile?.parentFile?.also { currentFile = it } != null) {
        if(currentFile == absoluteParent)
            return true
    }

    return false
}

