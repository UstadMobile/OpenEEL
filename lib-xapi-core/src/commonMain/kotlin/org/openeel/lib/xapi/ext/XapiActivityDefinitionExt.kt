package org.openeel.lib.xapi.ext

import io.ktor.http.Url
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.openeel.lib.xapi.OpenEelXapiConstants
import org.openeel.lib.xapi.model.XapiActivityDefinition
import kotlin.time.Instant

/**
 * Get the web pub manifest url as a string via the extension if present
 */
fun XapiActivityDefinition.webPubManifestOrNull(): String? {
    val jsonPrimitive = extensions?.get(
        OpenEelXapiConstants.ACTIVITY_EXTENSION_WEBPUB_MANIFEST_LINK
    ) as? JsonPrimitive
    return jsonPrimitive?.contentOrNull
}

/**
 * Get the web pub manifest url as a url via the extension if present and a valid url
 */
fun XapiActivityDefinition.webPubManifestAsUrlOrNull(): Url? {
    return webPubManifestOrNull()?.runCatching { Url(this) }?.getOrNull()
}

/**
 * Get the OPDS collection link (xAPI Activity Profile Resource url) as a string via the
 * extension if present, as per README_COLLECTIONS_LISTING_RECIPE.md
 */
fun XapiActivityDefinition.opdsCollectionLinkOrNull(): String? {
    val jsonPrimitive = extensions?.get(
        OpenEelXapiConstants.ACTIVITY_EXTENSION_OPDS_COLLECTION_LINK
    ) as? JsonPrimitive
    return jsonPrimitive?.contentOrNull
}

/**
 * Get the OPDS collection link as a Url via the extension if present and a valid url, as per
 * README_COLLECTIONS_LISTING_RECIPE.md
 */
fun XapiActivityDefinition.opdsCollectionLinkAsUrlOrNull(): Url? {
    return opdsCollectionLinkOrNull()?.runCatching { Url(this) }?.getOrNull()
}

/**
 * Get the deadline as a string via the extension if present
 */
fun XapiActivityDefinition.extensionDeadlineAsInstantOrNull(): Instant? {
    return extensions?.get(OpenEelXapiConstants.ACTIVITY_EXTENSION_DEADLINE)
        ?.takeIf { it is JsonPrimitive }?.jsonPrimitive?.contentOrNull?.let {
            try { Instant.parse(it) }
            catch (_: Throwable) { null }
        }
}

/**
 * Get the JsonObject for the given extension iri if present and a JsonObject
 */
fun XapiActivityDefinition.extensionAsJsonObjectOrNull(
    extensionIri: String,
): JsonObject? {
    return (extensions?.get(extensionIri) as? JsonObject)
}

/**
 * Deserialize (using kotlinx serialization) from an extension property
 *
 * @param json Json to use
 * @param extensionIri extension iri
 * @param deserializer kotlinx serialization deserializer
 *
 * @return the decoded object, or null if the extension is not present or the property is not a
 *         json object.
 */
fun <T> XapiActivityDefinition.decodeFromExtensionOrNull(
    json: Json,
    extensionIri: String,
    deserializer: DeserializationStrategy<T>,
): T? {
    return extensionAsJsonObjectOrNull(extensionIri)?.let {
        json.decodeFromJsonElement(deserializer, it)
    }
}


