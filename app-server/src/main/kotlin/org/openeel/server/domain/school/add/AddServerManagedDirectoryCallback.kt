package world.respect.server.domain.school.add

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.libxxhash.XXStringHasher
import kotlin.random.Random

/**
 * Used on the server side: Create a SchoolDirectoryEntity RESPECT Directory that is managed on this
 * server.
 */
class AddServerManagedDirectoryCallback(
    private val xxStringHasher: XXStringHasher,
): RoomDatabase.Callback() {

    override fun onCreate(connection: SQLiteConnection) {
        val randomInvitePrefix = Random.nextInt(1, MAX_INVITE_PREFIX)
        val invitePrefixStr = randomInvitePrefix.toString().padStart(
            MAX_INVITE_PREFIX.toString().length, '0'
        )

        connection.execSQL("""
            INSERT INTO SchoolDirectoryEntity(rdUid, rdUrl, rdInvitePrefix) 
            VALUES(
                ${xxStringHasher.hash(RespectSchoolDirectory.SERVER_MANAGED_DIRECTORY_URL)},
                '${RespectSchoolDirectory.SERVER_MANAGED_DIRECTORY_URL}',
                '$invitePrefixStr'
            )    
        """.trimIndent())
    }

    companion object {

        const val MAX_INVITE_PREFIX = 10_000
    }

}