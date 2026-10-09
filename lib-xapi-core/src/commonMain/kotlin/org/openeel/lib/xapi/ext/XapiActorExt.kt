package world.respect.lib.xapi.ext

import world.respect.lib.xapi.model.XapiActor
import world.respect.lib.xapi.model.XapiGroup

val XapiActor.idStr: String?
    get() = when {
        account != null -> "${account?.name}@${account?.homePage}"
        mbox != null -> mbox
        mbox_sha1sum != null -> mbox_sha1sum
        openid != null -> openid
        else -> null
    }

/**
 * The inverse functional identifier (IFI) to use for this actor. This is a string following the same
 * schema as SQL LRS. See:
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#2423-inverse-functional-identifier
 *
 * Each Agent or Identified Group must have one and only one IFI. An anonymous group will not have
 * an IFI.
 */
fun XapiActor.ifiOrNull(): String? {
    return when {
        mbox != null -> "mbox::$mbox"
        mbox_sha1sum != null -> "mbox_sha1sum::$mbox_sha1sum"
        openid != null -> "openid::$openid"
        account != null -> account?.let { "account::${it.name}@${it.homePage}" }
        else -> null
    }
}

/**
 * Require an inverse functional identifier (IFI) to use for this actor. This is a string following
 * the same schema as SQL LRS. See:
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#2423-inverse-functional-identifier
 *
 * Each Agent or Identified Group must have one and only one IFI. An anonymous group will not have
 * an IFI. If this is invoked on an anonymous group, an exception will be thrown.
 */
fun XapiActor.requireIfi(): String {
    return ifiOrNull() ?: throw IllegalStateException("requireIfi: $this has no IFI")
}


/**
 * A list of actors can include Agents, identified groups, and anonymous groups. The same actor may
 * be in the list more than once.
 *
 * The hazard: an identified group can be reference
 */
fun List<XapiActor>.distinctMerged(): List<XapiActor> {
    val actorsById = groupBy { it.idStr }
    return actorsById.map { (_, actors) ->
        val first = actors.first()
        val isIdentifiedGroup = first is XapiGroup && !first.isAnonymous
        val actorsAsGroups = if(isIdentifiedGroup) {
            actors.mapNotNull { it as? XapiGroup }
        }else {
            emptyList()
        }

        val hasMembersDefined = isIdentifiedGroup && actorsAsGroups.any { it.member != null }

        //If member is null, keep it null, so that the store routine knows
        //not to attempt to update the member list.
        if(isIdentifiedGroup && hasMembersDefined) {
            first.copy(
                member = actors.mapNotNull {
                    (it as? XapiGroup)?.member
                }.flatten().distinctBy { it.idStr }
            )
        }else {
            first
        }
    }
}