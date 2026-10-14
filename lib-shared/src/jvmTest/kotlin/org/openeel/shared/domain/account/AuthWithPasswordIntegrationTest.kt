package org.openeel.shared.domain.account

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.mock
import org.openeel.credentials.passkey.OpenEelPasswordCredential
import org.openeel.datalayer.db.SchoolDatabase
import org.openeel.datalayer.db.school.adapters.toEntities
import org.openeel.datalayer.school.model.Person
import org.openeel.libxxhash.XXStringHasher
import org.openeel.libxxhash.jvmimpl.XXStringHasherCommonJvm
import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.school.adapters.asEntity
import org.openeel.datalayer.shared.XXHashUidNumberMapper
import org.openeel.datalayer.school.model.PersonGenderEnum
import org.openeel.shared.domain.account.authwithpassword.GetTokenAndUserProfileWithCredentialDbImpl
import org.openeel.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithCredentialUseCase
import org.openeel.shared.domain.account.setpassword.EncryptPersonPasswordUseCase
import org.openeel.shared.domain.account.setpassword.EncryptPersonPasswordUseCaseImpl
import org.openeel.shared.domain.account.validateauth.ValidateAuthorizationUseCase
import org.openeel.shared.domain.account.validateauth.ValidateAuthorizationUseCaseDbImpl
import org.openeel.sharedse.domain.account.authenticatepassword.AuthenticatePasswordUseCaseDbImpl
import org.openeel.sharedse.domain.account.authenticatepassword.AuthenticateQrBadgeUseCaseDbImpl
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthWithPasswordIntegrationTest {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var schoolDb: SchoolDatabase

    private lateinit var xxHash: XXStringHasher

    private lateinit var uidNumberMapper: UidNumberMapper

    private lateinit var encryptPersonPasswordUseCase: EncryptPersonPasswordUseCase

    private lateinit var getTokenUseCase: GetTokenAndUserProfileWithCredentialUseCase

    private lateinit var validateAuthUseCase: ValidateAuthorizationUseCase

    private val defaultTestPerson = Person(
        guid = "42",
        username = "testuser",
        givenName = "John",
        familyName = "Doe",
        roles = emptyList(),
        gender = PersonGenderEnum.FEMALE,
    )

    private val defaultSchoolUrl = Url("https://school.example.org/")

    @BeforeTest
    fun setup() {
        val dbDir = temporaryFolder.newFolder("dbdir")
        schoolDb = Room.databaseBuilder<SchoolDatabase>(
            File(dbDir, "realm-test.db").absolutePath
        ).setDriver(BundledSQLiteDriver())
            .build()
        xxHash = XXStringHasherCommonJvm()
        uidNumberMapper = XXHashUidNumberMapper(xxHash)
        encryptPersonPasswordUseCase = EncryptPersonPasswordUseCaseImpl()
        getTokenUseCase = GetTokenAndUserProfileWithCredentialDbImpl(
            schoolUrl = defaultSchoolUrl,
            schoolDb = schoolDb,
            xxHash = xxHash,
            verifyPasskeyUseCase = mock { },
            schoolDirectoryDataSource = mock { },
            authenticatePasswordUseCase = AuthenticatePasswordUseCaseDbImpl(
                schoolDb = schoolDb,
                encryptPersonPasswordUseCase = EncryptPersonPasswordUseCaseImpl(),
                uidNumberMapper = uidNumberMapper,
            ),
            authenticateQrBadgeUseCase = AuthenticateQrBadgeUseCaseDbImpl(
                schoolDb = schoolDb,
                uidNumberMapper = uidNumberMapper,
            )
        )

        validateAuthUseCase = ValidateAuthorizationUseCaseDbImpl(schoolDb)
    }

    @Test
    fun givenAuthSet_whenAuthWithPasswordInvoked_thenWillReturnToken() {
        runBlocking {
            val personGuid = "42"
            val password = "password"

            schoolDb.getPersonEntityDao().insert(
                defaultTestPerson.toEntities(uidNumberMapper).personEntity
            )

            schoolDb.getPersonPasswordEntityDao().upsert(
                encryptPersonPasswordUseCase(
                    EncryptPersonPasswordUseCase.Request(
                        personGuid = personGuid,
                        password = password,
                    )
                ).asEntity(uidNumberMapper)
            )

            val authResponse = getTokenUseCase(
                OpenEelPasswordCredential(defaultTestPerson.username!!, password)
            )

            val userIdPrincipal = validateAuthUseCase(
                ValidateAuthorizationUseCase.BearerTokenCredential(
                    token = authResponse.token.accessToken
                )
            )

            assertEquals(authResponse.person.guid, personGuid)
            assertEquals(defaultTestPerson.guid, userIdPrincipal!!.guid)
        }
    }

    @Test
    fun givenAuthSet_whenAuthPasswordInvokedWithWRongPass_thenWillThrowException() {
        runBlocking {
            var exception: Throwable? = null
            try {
                val personGuid = "42"
                val password = "password"
                schoolDb.getPersonEntityDao().insert(
                    defaultTestPerson.toEntities(uidNumberMapper).personEntity
                )

                schoolDb.getPersonPasswordEntityDao().upsert(
                    encryptPersonPasswordUseCase(
                        EncryptPersonPasswordUseCase.Request(
                            personGuid = personGuid,
                            password = password,
                        )
                    ).asEntity(uidNumberMapper)
                )

                getTokenUseCase(
                    OpenEelPasswordCredential(defaultTestPerson.username!!, "wrong")
                )
            }catch(e: Throwable) {
                exception = e
            }

            assertNotNull(exception)
        }
    }

}