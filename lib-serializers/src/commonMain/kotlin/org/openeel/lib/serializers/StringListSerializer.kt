package world.respect.lib.serializers

import kotlinx.serialization.builtins.serializer

/**
 * Where a JSON specification allows a String or List of String, the model class can use
 * type List<String> and then this serializer will automatically convert a single String to
 * a singleton list.
 */
object StringListSerializer: SingleItemToListTransformer<String>(String.serializer())
