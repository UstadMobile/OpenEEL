package world.respect.testutil

import java.io.File
import java.io.FileFilter

fun File.recursiveFindAndReplace(
    fileFilter: FileFilter,
    textReplacement: (String) -> String,
) {
    listFiles(fileFilter)?.forEach {
        val text = it.readText()
        it.writeText(textReplacement(text))
    }

    this.listFiles( { file: File -> file.isDirectory })?.forEach { childDir ->
        childDir.recursiveFindAndReplace(fileFilter, textReplacement)
    }

}