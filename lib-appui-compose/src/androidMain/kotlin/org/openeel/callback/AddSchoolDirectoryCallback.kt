package org.openeel.callback

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import org.openeel.libxxhash.XXStringHasher
import java.util.Properties

class AddSchoolDirectoryCallback(
    private val addDirectoriesFromPropertiesUseCase: AddDirectoriesFromPropertiesUseCase,
) : RoomDatabase.Callback() {

    override fun onCreate(connection: SQLiteConnection) {
        super.onCreate(connection)
        addDirectoriesFromPropertiesUseCase().forEach {
            connection.execSQL(it)
        }
    }
}
