package world.respect.datalayer.school.xapi.ext

import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiActivityDefinition
import world.respect.lib.xapi.model.XapiObjectType



/**
 * A single statement can have multiple references to the same activity. Some references may, or may
 * not, include all parts of the definition. This function will merge the definition. In case of
 * a conflict (e.g. where the name langmap has two different entries for the same language), there
 * is no defined behavior. That should not happen. This is intended to work on handling the data
 * within one transaction.
 */
fun List<XapiActivity>.distinctMerged(): List<XapiActivity> {
    return groupBy { it.id }.map { (id, activities) ->
        if (activities.size == 1) {
            activities.first()
        }else {
            val definitions = activities.mapNotNull { it.definition }

            XapiActivity(
                objectType = XapiObjectType.Activity,
                id = id,
                definition = XapiActivityDefinition(
                    name = definitions.mapNotNull { it.name }.mergeLangMap(),
                    description = definitions.mapNotNull { it.description }.mergeLangMap(),
                    type = definitions.firstNotNullOfOrNull { it.type },
                    extensions = buildMap {
                        definitions.forEach { definition ->
                            definition.extensions?.also { putAll(it) }
                        }
                    },
                    moreInfo = definitions.firstNotNullOfOrNull { it.moreInfo },
                    interactionType = definitions.firstNotNullOfOrNull { it.interactionType },
                    correctResponsesPattern = definitions.firstNotNullOfOrNull {
                        it.correctResponsesPattern
                    },
                    choices = definitions.firstNotNullOfOrNull { it.choices },
                    scale = definitions.firstNotNullOfOrNull { it.scale },
                    source = definitions.firstNotNullOfOrNull { it.source },
                    target = definitions.firstNotNullOfOrNull { it.target },
                    steps = definitions.firstNotNullOfOrNull { it.steps },
                )
            )
        }
    }
}