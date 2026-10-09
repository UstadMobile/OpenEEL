package world.respect.datalayer.db.school.xapi

import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement

/**
 * As per
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#41-extensions
 *
 * Extensions are a map where the key is an IRI.
 */
internal val xapiExtensionsSerializer = MapSerializer(
    String.serializer(), JsonElement.serializer()
)