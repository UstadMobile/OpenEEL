package org.openeel.shared.domain.account

import kotlinx.serialization.Serializable
import org.openeel.datalayer.school.ext.asXapiAgent
import org.openeel.datalayer.school.model.Person
import org.openeel.lib.xapi.model.XapiAgent

/**
 *
 * @property session the session that includes the [UserAccount] and active person uid
 * @property person the person that is the active person for the session
 * @property relatedPersons where the [UserAccount]'s related personUid has related personUids as
 *           per Person.relatedPersonUids (eg a parents' account), then the relatedPersons are all
 *           those returned by PersonDataSource.list (common.guid = session.account.userGuid,
 *           includeRelated=true).
 *
 *           If the (active) person is not the account holder themselves, then the account holder
 *           themselves will be in the relatedPersons list.
 *
 * @property xapiAgent the XapiAgent for the currently active person.
 *
 */
@Serializable
data class UserSessionAndPerson(
    val session: UserSession,
    val person: Person,
    val relatedPersons: List<Person> = emptyList(),
){
    val isChild: Boolean
        get() = session.account.userGuid != person.guid

    val xapiAgent: XapiAgent
        get() = person.asXapiAgent(session.account.school.self)

}
