package world.respect.lib.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer

/**
 * Where a JSON specification allows a single item or list of items of a given type, this serializer
 * will automatically convert a single item to a list of one item.
 */
abstract class SingleItemToListTransformer<T: Any>(
    serializer: KSerializer<T>
): JsonTransformingSerializer<List<T>>(ListSerializer(serializer)) {

    override fun transformDeserialize(element: JsonElement): JsonElement {
        return super.transformDeserialize(
            if(element !is JsonArray) {
                JsonArray(listOf(element))
            }else {
                element
            }
        )
    }
}

