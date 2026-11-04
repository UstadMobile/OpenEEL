package org.openeel.datalayer.ext

object DatabaseType {
    const val SQLITE = 1
    fun getCurrentDbType(): Int = SQLITE
}

fun Any.getDbType(): Int = DatabaseType.SQLITE