package world.respect.datalayer.db.shared

import androidx.room.TypeConverter
import com.eygraber.uri.Uri
import io.ktor.http.Url
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import world.respect.datalayer.db.shared.entities.LangMapEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

class SharedConverters {

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let { Json.decodeFromString(it) }
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun fromLangMapTopParentType(value: LangMapEntity.TopParentType): Int {
        return value.id
    }

    @TypeConverter
    fun toLangMapTopParentType(value: Int): LangMapEntity.TopParentType {
        return LangMapEntity.TopParentType.entries.first { it.id == value }
    }

    @TypeConverter
    fun fromLangMapPropType(value: LangMapEntity.PropType): Int {
        return value.id
    }

    @TypeConverter
    fun toLangMapPropType(value: Int): LangMapEntity.PropType {
        return LangMapEntity.PropType.entries.first { it.id == value }
    }

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun fromUri(value: Uri?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toUri(value: String?): Uri? {
        return value?.let { Uri.parse(it) }
    }

    @TypeConverter
    fun fromUrl(value: Url?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toUrl(value: String?): Url? {
        return value?.let { Url(it) }
    }

    @TypeConverter
    fun fromInstant(value: Instant?): Long? {
        return value?.toEpochMilliseconds()
    }

    @TypeConverter
    fun toInstant(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it) }
    }

    @TypeConverter
    fun fromInstantAsTimestampString(value: InstantAsTimestampString?): String? {
        return value?.instant?.toString()
    }

    @TypeConverter
    fun toInstantAsTimestampString(value: String?): InstantAsTimestampString? {
        return value?.let { InstantAsTimestampString(Instant.parse(it)) }
    }

    @TypeConverter
    fun fromUuid(value: Uuid?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toUuid(value: String?): Uuid? {
        return value?.let { Uuid.parse(it) }
    }

}