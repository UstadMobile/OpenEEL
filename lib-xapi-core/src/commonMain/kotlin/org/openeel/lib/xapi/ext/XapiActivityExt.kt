package world.respect.lib.xapi.ext

import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiActivityDefinition

fun List<XapiActivity>.addOrReplaceById(
    other: XapiActivity
) : List<XapiActivity> {
    val index = indexOfFirst { it.id == other.id }
    return if(index > 0) {
        this.toMutableList().also {
            it[index] = other
        }.toList()
    }else{
        this + other
    }
}

fun XapiActivity.definitionOrBlank(): XapiActivityDefinition {
    return this.definition ?: XapiActivityDefinition()
}

/**
 * Returns a copy of the activity with the definition name updated.
 */
fun XapiActivity.copyWithDefinitionName(
    name: Map<String, String>
): XapiActivity {
    return this.copy(definition = definitionOrBlank().copy(name = name))
}

/**
 * Returns a copy of the activity with the definition description updated.
 */
fun XapiActivity.copyWithDefinitionDescription(
    description: Map<String, String>
): XapiActivity {
    return this.copy(definition = definitionOrBlank().copy(description = description))
}
