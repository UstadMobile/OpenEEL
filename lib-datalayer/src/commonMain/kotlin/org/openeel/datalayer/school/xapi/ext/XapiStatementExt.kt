package world.respect.datalayer.school.xapi.ext

import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import world.respect.lib.xapi.ext.objectActivityOrNull
import world.respect.lib.xapi.model.XAPI_PROGRESSED_EXTENSIONS
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiActor
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb
import kotlin.uuid.Uuid

val XapiStatement.resultProgressExtension: Int?
    get() = result?.extensions?.let { extensions ->
        XAPI_PROGRESSED_EXTENSIONS.firstNotNullOfOrNull { extensionKey ->
            extensions.get(extensionKey)?.jsonPrimitive?.intOrNull
        }
    }


/**
 * Shorthand to determine if this statement contains completion or progress data.
 */
fun XapiStatement.isCompletionOrProgress(): Boolean {
    return result?.completion != null || resultProgressExtension != null
}

/**
 * Get a list of all actors referenced in this statement or its substatement including:
 * The statement actor itself
 * The object if it is an actor
 * Actors from a substatement
 * The team and instructor actor from context
 * The authority actor
 */
fun XapiStatement.allActors(): List<XapiActor> {
    return buildList {
        add(actor)
        (`object` as? XapiActor)?.also { add(it) }
        (`object` as? XapiStatement)?.also {
            addAll(it.allActors())
        }
        context?.team?.also { add(it) }
        context?.instructor?.also { add(it) }
        authority?.also { add(it) }
    }
}

/**
 * Get a list of all the activities with a definition related to the statement (e.g. those that
 * will need to be stored in the database's Activity table).
 */
fun XapiStatement.allDefinedActivities(): List<XapiActivity> {
    return buildList {
        fun addAllDefinedActivitiesInternal(list: List<XapiActivity>?) {
            list?.filter { it.definition != null }?.also { addAll(it) }
        }

        (`object` as? XapiActivity)?.takeIf { it.definition != null }?.also { add(it) }

        (`object` as? XapiStatement)?.also { subStatement ->
            addAll(subStatement.allDefinedActivities())
        }

        addAllDefinedActivitiesInternal(context?.contextActivities?.parent)
        addAllDefinedActivitiesInternal(context?.contextActivities?.grouping)
        addAllDefinedActivitiesInternal(context?.contextActivities?.category)
        addAllDefinedActivitiesInternal(context?.contextActivities?.other)
    }
}

fun XapiStatement.allActivities(): List<XapiActivity> {
    return buildList {
        objectActivityOrNull()?.also { add(it) }
        context?.contextActivities?.also { ctxActivities ->
            ctxActivities.parent?.also { addAll(it) }
            ctxActivities.grouping?.also { addAll(it) }
            ctxActivities.category?.also { addAll(it) }
            ctxActivities.other?.also { addAll(it) }
        }
    }
}


/**
 * List of all verbs that have any properties defined (e.g. a display name). Exclude those that are
 * id only
 */
fun XapiStatement.allDefinedVerbs(): List<XapiVerb> {
    return buildList {
        verb.display?.also { add(verb) }
        (`object` as? XapiStatement)?.also {
            addAll(it.allDefinedVerbs())
        }
    }
}

fun XapiStatement.allVerbs(): List<XapiVerb> {
    return buildList {
        add(verb)
        (`object` as? XapiStatement)?.also {
            addAll(it.allVerbs())
        }
    }
}

fun XapiStatement.copyWithIdIfNotSet() : XapiStatement {
    val stmtId = id
    return if(stmtId != null) {
        this
    }else {
        copy(id = Uuid.random())
    }
}
