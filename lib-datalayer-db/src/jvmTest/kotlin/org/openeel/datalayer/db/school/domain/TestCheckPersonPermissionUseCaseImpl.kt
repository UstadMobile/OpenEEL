package org.openeel.datalayer.db.school.domain

import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.datalayer.db.school.insertAdmin
import org.openeel.datalayer.db.school.testSchoolDb
import org.openeel.datalayer.db.school.toDataSource
import org.openeel.datalayer.school.domain.CheckPersonPermissionUseCase
import org.openeel.datalayer.school.ext.primaryRole
import org.openeel.datalayer.school.model.Person
import org.openeel.datalayer.school.model.PersonGenderEnum
import org.openeel.datalayer.school.model.PersonRole
import org.openeel.app.userdirectory.model.PersonRoleEnum
import org.openeel.datalayer.shared.XXHashUidNumberMapper
import org.openeel.libxxhash.jvmimpl.XXStringHasherCommonJvm
import kotlin.test.Test
import kotlin.test.assertFalse

class TestCheckPersonPermissionUseCaseImpl {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun givenAuthenticatedUserIsStudent_whenCheckWritePermissionForTeacher_thenReturnsFalse() {
        runBlocking {
            testSchoolDb(temporaryFolder.newFolder()) { db ->
                val adminUid = "1"
                val studentUid = "2"
                val adminSchoolDs = db.toDataSource(
                    authenticatedUserUid = adminUid,
                    schoolUrl = Url("http://localhost:8098/"),
                )

                val adminPerson = adminSchoolDs.insertAdmin(adminUid)
                AddDefaultSchoolPermissionGrantsUseCase(
                    schoolDb = db,
                    uidNumberMapper = XXHashUidNumberMapper(XXStringHasherCommonJvm())
                ).invoke()
                adminSchoolDs.personDataSource.store(
                    listOf(
                        Person(
                            guid = studentUid,
                            givenName = "Student",
                            familyName = "User",
                            gender = PersonGenderEnum.FEMALE,
                            roles = listOf(PersonRole(true, PersonRoleEnum.STUDENT)),
                        )
                    )
                )


                val checkPersonUseCase = CheckPersonPermissionUseCaseDbImpl(
                    authenticatedUser = AuthenticatedUserPrincipalId(studentUid),
                    schoolDb = db,
                    uidNumberMapper = XXHashUidNumberMapper(XXStringHasherCommonJvm())
                )

                assertFalse(
                    checkPersonUseCase(
                        otherPersonUid = adminPerson.guid,
                        otherPersonKnownRole = adminPerson.primaryRole(),
                        permissionsRequiredByRole = CheckPersonPermissionUseCase.PermissionsRequiredByRole.WRITE_PERMISSIONS,
                    )
                )
            }
        }
    }

}