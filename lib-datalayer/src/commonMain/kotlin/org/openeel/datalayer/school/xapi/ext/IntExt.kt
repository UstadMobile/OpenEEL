package world.respect.datalayer.school.xapi.ext

fun Int.hasFlag(flag: Int): Boolean = (this and flag) == flag
