package org.openeel.lib.opds.model

import com.eygraber.uri.Uri
import kotlinx.serialization.Serializable
import org.openeel.lib.serializers.SingleItemToListTransformer
import org.openeel.lib.serializers.StringOrObjectSerializer
import org.openeel.lib.serializers.StringValue
import org.openeel.lib.serializers.StringValueSerializer
import org.openeel.lib.serializers.UriStringSerializer

/**
 * Schema: https://readium.org/webpub-manifest/schema/subject.schema.json
 */
@Serializable(with = ReadiumSubjectSerializer::class)
sealed class ReadiumSubject

@Serializable(with = ReadiumSubjectStringValueSerializer::class)
data class ReadiumSubjectStringValue(override val value: String): ReadiumSubject(), StringValue

object ReadiumSubjectStringValueSerializer: StringValueSerializer<ReadiumSubjectStringValue>(
    serialName = "ReadiumSubjectStringValue", stringToValue = {
        ReadiumSubjectStringValue(it)
    }
)

/**
 * Schema: https://readium.org/webpub-manifest/schema/subject-object.schema.json
 */
@Serializable
data class ReadiumSubjectObject(
    val name: LangMap,
    val sortAs: String? = null,
    val code: String? = null,
    @Serializable(with = UriStringSerializer::class)
    val scheme: Uri? = null,
    val links: List<ReadiumLink>? = null,
): ReadiumSubject()

object ReadiumSubjectSerializer: StringOrObjectSerializer<ReadiumSubject>(
    ReadiumSubject::class,
    primitiveSerializer = ReadiumSubjectStringValue.serializer(),
    objectSerializer = ReadiumSubjectObject.serializer()
)

object ReadiumSubjectToListTransformer: SingleItemToListTransformer<ReadiumSubject>(
    ReadiumSubject.serializer()
)

/**
 * Extension property to get the name of a [ReadiumSubject] as a [LangMap].
 */
val ReadiumSubject.name: LangMap
    get() = when (this) {
        is ReadiumSubjectObject -> name
        is ReadiumSubjectStringValue -> LangMapStringValue(value)
    }
