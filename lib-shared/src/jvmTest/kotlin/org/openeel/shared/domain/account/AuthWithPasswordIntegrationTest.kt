package world.respect.shared.domain.account

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.mock
import world.respect.credentials.passkey.RespectPasswordCredential
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toEntities
import world.respect.datalayer.school.model.Person
import world.respect.libxxhash.XXStringHasher
import world.respect.libxxhash.jvmimpl.XXStringHasherCommonJvm
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.adapters.asEntity
import world.respect.datalayer.shared.XXHashUidNumberMapper
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.shared.domain.account.authwithpassword.GetTokenAndUserProfileWithCredentialDbImpl
import world.respect.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithCredentialUseCase
import world.respect.shared.domain.account.setpassword.EncryptPersonPasswordUseCase
import world.respect.shared.domain.account.setpassword.EncryptPersonPasswordUseCaseImpl
import world.respect.shared.domain.account.validateauth.ValidateAuthorizationUseCase
import world.respect.shared.domain.account.validateauth.ValidateAuthorizationUseCaseDbImpl
import world.respect.sharedse.domain.account.authenticatepassword.AuthenticatePasswordUseCaseDbImpl
import world.respect.sharedse.domain.account.authenticatepassword.AuthenticateQrBadgeUseCaseDbImpl
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthWithPasswordIntegrationTest {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var schoolDb: RespectSchoolDatabase

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
        schoolDb = Room.databaseBuilder<RespectSchoolDatabase>(
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
            respectAppDataSource = mock { },
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
                RespectPasswordCredential(defaultTestPerson.username!!, password)
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
                    RespectPasswordCredential(defaultTestPerson.username!!, "wrong")
                )
            }catch(e: Throwable) {
                exception = e
            }

            assertNotNull(exception)
        }
    }

}