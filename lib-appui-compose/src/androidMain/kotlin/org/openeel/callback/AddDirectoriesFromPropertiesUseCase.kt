package world.respect.callback

import world.respect.libxxhash.XXStringHasher
import java.util.Properties

class AddDirectoriesFromPropertiesUseCase(
    private val xxStringHasher: XXStringHasher,
) {

    operator fun invoke() : List<String> {
        val props = Properties()

        val defaultStream = javaClass.classLoader
            ?.getResourceAsStream("directories/default.properties")
            ?: throw IllegalStateException("default.properties not found in resources")
        defaultStream.use { props.load(it) }

        val localStream = javaClass.classLoader
            ?.getResourceAsStream("directories/local.properties")
        localStream?.use {
            props.load(it)
        }


        return props.entries.map { entry ->
            val url = entry.value.toString()
            val uid = xxStringHasher.hash(url)
            val prefix = entry.key.toString()
            val name = entry.key.toString()

            """
            INSERT OR IGNORE INTO SchoolDirectoryEntity(rdUid, rdUrl, rdInvitePrefix, rdName) 
            VALUES('$uid','$url','$prefix', '$name')
            """.trimIndent()

        }
    }
}