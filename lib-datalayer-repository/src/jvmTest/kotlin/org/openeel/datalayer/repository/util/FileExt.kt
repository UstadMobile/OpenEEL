package world.respect.datalayer.repository.util

import java.io.File

fun File.mkdirsIfNotExists(): File {
    if(!exists())
        mkdirs()

    return this
}