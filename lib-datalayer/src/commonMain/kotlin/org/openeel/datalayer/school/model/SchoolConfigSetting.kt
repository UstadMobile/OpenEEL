package org.openeel.datalayer.school.model

import kotlinx.serialization.Serializable
import org.openeel.app.userdirectory.model.PersonRoleEnum
import org.openeel.datalayer.shared.ModelWithTimes
import org.openeel.lib.serializers.InstantAsISO8601
import kotlin.time.Clock

/**
 * Provides a way to store key-value configuration settings for the school itself e.g. the list of
 * URLs of app catalogs, single sign-on settings, etc).
 *
 * Config settings can only be written by the admin. Storing each key-value pair as its own entity
 * allows for granular per-setting control over read permissions.
 */
@Serializable
data class SchoolConfigSetting(
    val key: String,
    val value: String,
    val status: StatusEnum = StatusEnum.ACTIVE,
    override val lastModified: InstantAsISO8601 = Clock.System.now(),
    override val stored: InstantAsISO8601 = Clock.System.now(),
    val canRead: List<PersonRoleEnum?> = listOf(),
) : ModelWithTimes {

    companion object {

        const val TABLE_ID = 84

    }
}