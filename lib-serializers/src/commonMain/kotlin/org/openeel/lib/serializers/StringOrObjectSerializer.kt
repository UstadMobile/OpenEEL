package world.respect.lib.serializers

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.reflect.KClass

/**
 * Handles polymorphic serialization where a value can be an object or primitive type.
 */
abstract class StringOrObjectSerializer<T: Any>(
    baseClass: KClass<T>,
    private val primitiveSerializer: KSerializer<out T>,
    private val objectSerializer: KSerializer<out T>
): JsonContentPolymorphicSerializer<T>(baseClass) {

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<T> {
        return when(element) {
            is JsonPrimitive -> primitiveSerializer
            else -> objectSerializer
        }
    }
}