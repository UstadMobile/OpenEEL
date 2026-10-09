package world.respect.callback

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

fun migrate6to8AddDirectories(
    addDirectoriesFromPropertiesUseCase: AddDirectoriesFromPropertiesUseCase
) : Migration {
    return object: Migration(6, 8) {
        override fun migrate(connection: SQLiteConnection) {
            addDirectoriesFromPropertiesUseCase().forEach {
                connection.execSQL(it)
            }
            connection.execSQL("DELETE FROM SchoolDirectoryEntity WHERE rdUrl = 'https://onrespect.app/'")
        }
    }
}