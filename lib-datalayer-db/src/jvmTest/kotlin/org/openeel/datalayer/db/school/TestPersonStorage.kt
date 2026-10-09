package world.respect.datalayer.db.school

import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.datalayer.db.school.domain.AddDefaultSchoolPermissionGrantsUseCase
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.datalayer.school.model.PersonRole
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.shared.XXHashUidNumberMapper
import world.respect.libxxhash.jvmimpl.XXStringHasherCommonJvm
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class TestPersonStorage {


    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun givenPersonStoredWithRolesAndRelatedPersons_whenStored_thenShouldMatch() {
        val adminUid = "1"
        val parentGuid = "2"
        val childGuid = "3"

        runBlocking {
            testSchoolDb(temporaryFolder.newFolder()) { db ->
                val schoolDs = db.toDataSource(
                    authenticatedUserUid = adminUid,
                    schoolUrl = Url("http://localhost:8098/"),
                )

                val parentPerson = Person(
                    guid = parentGuid,
                    givenName = "Parent",
                    familyName = "Person",
                    gender = PersonGenderEnum.FEMALE,
                    roles = listOf(PersonRole(true, PersonRoleEnum.PARENT)),
                    relatedPersonUids = listOf(childGuid)
                )

                val childPerson = Person(
                    guid = childGuid,
                    givenName = "Child",
                    familyName = "Person",
                    gender = PersonGenderEnum.FEMALE,
                    roles = listOf(PersonRole(true, PersonRoleEnum.STUDENT)),
                    relatedPersonUids = listOf(parentGuid),
                )

                fun assertDetailsMatch(expected: Person, actual: Person?) {
                    assertContentEquals(expected.relatedPersonUids, actual?.relatedPersonUids)
                    assertEquals(expected.givenName, actual?.givenName)
                    assertContentEquals(expected.roles, actual?.roles)
                }


                schoolDs.insertAdmin(adminUid)
                AddDefaultSchoolPermissionGrantsUseCase(
                    db, XXHashUidNumberMapper(XXStringHasherCommonJvm())
                ).invoke()
                schoolDs.personDataSource.store(listOf(parentPerson, childPerson))
                val parentFromDb = schoolDs.personDataSource.findByGuid(DataLoadParams(), parentGuid).dataOrNull()
                val childFromDb = schoolDs.personDataSource.findByGuid(DataLoadParams(), childGuid).dataOrNull()
                assertDetailsMatch(parentPerson, parentFromDb)
                assertDetailsMatch(childPerson, childFromDb)

            }
        }
    }


}