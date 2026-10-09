package world.respect.server
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.core.XmlVersion
import nl.adaptivity.xmlutil.serialization.XML
import org.koin.core.scope.Scope
import org.koin.dsl.module
import world.respect.credentials.passkey.request.DecodeUserHandleUseCase
import world.respect.credentials.passkey.request.GetPasskeyProviderInfoUseCase
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.RespectAppDataSourceLocal
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.APP_MIGRATION_8_9_SERVER
import world.respect.datalayer.db.RespectAppDataSourceDb
import world.respect.datalayer.db.RespectAppDatabase
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.SchoolDataSourceDb
import world.respect.datalayer.db.addCommonMigrations
import world.respect.datalayer.db.school.domain.AddDefaultSchoolPermissionGrantsUseCase
import world.respect.datalayer.db.school.domain.CheckPersonPermissionUseCaseDbImpl
import world.respect.datalayer.db.school.domain.GetPermissionLastModifiedUseCaseDbImpl
import world.respect.datalayer.db.schooldirectory.SchoolDirectoryDataSourceDb
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.school.domain.CheckPersonPermissionUseCase
import world.respect.datalayer.school.domain.GetPermissionLastModifiedUseCase
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSourceLocal
import world.respect.datalayer.shared.XXHashUidNumberMapper
import world.respect.lib.primarykeygen.PrimaryKeyGenerator
import world.respect.libutil.ext.sanitizedForFilename
import world.respect.libxxhash.XXStringHasher
import world.respect.libxxhash.jvmimpl.XXStringHasherCommonJvm
import world.respect.server.account.invite.GetInviteInfoUseCaseServer
import world.respect.server.account.invite.username.UsernameSuggestionUseCaseServer
import world.respect.server.account.invite.username.checkusernameunique.CheckUsernameUniqueUseCaseServer
import world.respect.shared.domain.account.passkey.VerifySignInWithPasskeyUseCase
import world.respect.server.domain.school.add.AddSchoolUseCase
import world.respect.server.domain.school.add.AddServerManagedDirectoryCallback
import world.respect.server.domain.school.add.RegisterSchoolUseCaseImpl
import world.respect.server.domain.school.demoapp.DemoStringMaps
import world.respect.server.domain.school.demoapp.MakeDemoAppCollectionUseCase
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase
import world.respect.server.domain.school.demoapp.MakeDemoAppLearningUnitManifestUseCase
import world.respect.server.domain.school.demoapp.MakeDemoAppManifestUseCase
import world.respect.server.domain.school.demoapp.MakeDemoAppLearningUnitTinCanXmlUseCase
import world.respect.server.domain.school.verify.VerifySchoolUrlPointsToThisServerUseCase
import world.respect.server.util.SchoolUrlVerificationManager
import world.respect.shared.domain.account.RespectAccount
import world.respect.shared.domain.account.authenticatepassword.AuthenticatePasswordUseCase
import world.respect.shared.domain.account.authenticatepassword.AuthenticateQrBadgeUseCase
import world.respect.shared.domain.account.authwithpassword.GetTokenAndUserProfileWithCredentialDbImpl
import world.respect.shared.domain.account.child.AddChildAccountUseCase
import world.respect.shared.domain.account.child.AddChildAccountUseCaseDb
import world.respect.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithCredentialUseCase
import world.respect.shared.domain.account.invite.CreateInviteUseCase
import world.respect.shared.domain.account.invite.CreateInviteUseCaseDb
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase
import world.respect.shared.domain.account.invite.RedeemInviteUseCase
import world.respect.shared.domain.account.invite.RedeemInviteUseCaseDb
import world.respect.shared.domain.account.passkey.DecodeUserHandleUseCaseImpl
import world.respect.shared.domain.account.passkey.GetPasskeyProviderInfoUseCaseImpl
import world.respect.shared.domain.account.passkey.GetActivePersonPasskeysDbImpl
import world.respect.shared.domain.account.passkey.GetActivePersonPasskeysUseCase
import world.respect.shared.domain.account.passkey.LoadAaguidJsonUseCase
import world.respect.shared.domain.account.passkey.LoadAaguidJsonUseCaseJvm
import world.respect.shared.domain.account.passkey.RevokePasskeyUseCase
import world.respect.shared.domain.account.passkey.RevokePersonPasskeyUseCaseDbImpl
import world.respect.shared.domain.account.setpassword.EncryptPersonPasswordUseCase
import world.respect.shared.domain.account.setpassword.EncryptPersonPasswordUseCaseImpl
import world.respect.shared.domain.account.sharedschooldevice.setpin.GetSharedDevicePINUseCase
import world.respect.shared.domain.account.sharedschooldevice.setpin.GetSharedDevicePINUseCaseImpl
import world.respect.shared.domain.account.sharedschooldevice.setpin.SetSharedDevicePINUseCase
import world.respect.shared.domain.account.sharedschooldevice.setpin.SetSharedDevicePINUseCaseImpl
import world.respect.shared.domain.account.username.UsernameSuggestionUseCase
import world.respect.shared.domain.account.username.checkusernameunique.CheckUsernameUniqueUseCase
import world.respect.shared.domain.account.username.filterusername.FilterUsernameUseCase
import world.respect.shared.domain.account.validateauth.ValidateAuthorizationUseCase
import world.respect.shared.domain.account.validateauth.ValidateAuthorizationUseCaseDbImpl
import world.respect.shared.domain.createlink.CreateInviteLinkUseCase
import world.respect.shared.domain.enrollments.UpdateClazzStudentXapiGroupUseCase
import world.respect.shared.domain.navigation.deeplink.UrlToCustomDeepLinkUseCase
import world.respect.shared.domain.school.RespectSchoolPath
import world.respect.shared.domain.school.SchoolPrimaryKeyGenerator
import world.respect.shared.domain.school.add.RegisterSchoolUseCase
import world.respect.shared.util.di.RespectAccountScopeId
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId
import world.respect.sharedse.domain.account.authenticatepassword.AuthenticatePasswordUseCaseDbImpl
import world.respect.sharedse.domain.account.authenticatepassword.AuthenticateQrBadgeUseCaseDbImpl
import java.io.File

const val APP_DB_FILENAME = "respect-app.db"
const val CUSTOM_PROTO = "world.respect.app"

fun serverKoinModule(
    config: ApplicationConfig,
    dataDir: File = config.absoluteDataDir()
) = module {

    single<RespectAppDatabase> {
        val dbFile = File(dataDir, APP_DB_FILENAME)
        Room.databaseBuilder<RespectAppDatabase>(dbFile.absolutePath)
            .setDriver(BundledSQLiteDriver())
            .addCallback(AddServerManagedDirectoryCallback(xxStringHasher = get()))
            .addCommonMigrations()
            .addMigrations(
                object: Migration(6, 8) {
                    override fun migrate(connection: SQLiteConnection) {
                        //do nothing on server.
                    }
                }
            )
            .addMigrations(APP_MIGRATION_8_9_SERVER)
            .build()
    }

    single<Json> {
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
        }
    }

    single<XML> {
        XML.v1 {
            recommended_1_0_0()
            xmlVersion = XmlVersion.XML10
        }
    }

    single<SchoolConfig> {
        SchoolConfig.fromConfig(config)
    }

    single<XXStringHasher> {
        XXStringHasherCommonJvm()
    }

    single<UidNumberMapper> {
        XXHashUidNumberMapper(xxStringHasher = get())
    }

    single<SchoolDirectoryDataSourceLocal> {
        SchoolDirectoryDataSourceDb(
            respectAppDb = get(),
            xxStringHasher = get()
        )
    }

    single<RespectAppDataSourceLocal> {
        RespectAppDataSourceDb(
            respectAppDatabase = get(),
            json = get(),
            xxStringHasher = get(),
        )
    }

    single<RespectAppDataSource> {
        get<RespectAppDataSourceLocal>()
    }

    single<FilterUsernameUseCase> {
        FilterUsernameUseCase()
    }

    single<AddSchoolUseCase> {
        AddSchoolUseCase(
            directoryDataSource = get<RespectAppDataSourceLocal>().schoolDirectoryDataSource,
            schoolDirectoryEntryDataSource = get<RespectAppDataSourceLocal>().schoolDirectoryEntryDataSource,
            encryptPasswordUseCase = get(),
        )
    }
    single<RegisterSchoolUseCase> {
        RegisterSchoolUseCaseImpl(
            registerSchoolPin = config.propertyOrNull(
                SERVER_CONFIG_KEY_REGISTRATION_PIN
            )?.getString()
        )
    }
    single<SchoolUrlVerificationManager> {
        SchoolUrlVerificationManager()
    }
    single<HttpClient> {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(json = get())
            }
        }
    }
    single<VerifySchoolUrlPointsToThisServerUseCase> {
        VerifySchoolUrlPointsToThisServerUseCase(
            verificationManager = get(),
            httpClient = get()
        )
    }
    single<DecodeUserHandleUseCase> {
        DecodeUserHandleUseCaseImpl()
    }
    single<UrlToCustomDeepLinkUseCase> {
        UrlToCustomDeepLinkUseCase(customProtocol = CUSTOM_PROTO)
    }
    single<LoadAaguidJsonUseCase> {
        LoadAaguidJsonUseCaseJvm(
            json = get(),
        )
    }

    single<GetPasskeyProviderInfoUseCase> {
        GetPasskeyProviderInfoUseCaseImpl(
            json = get(),
            loadAaguidJsonUseCase = get(),
        )
    }

    single<EncryptPersonPasswordUseCase> {
        EncryptPersonPasswordUseCaseImpl()
    }


    single<DemoStringMaps> {
        DemoStringMaps.initFromResources(json = get())
    }

    single<MakeDemoAppManifestUseCase> {
        MakeDemoAppManifestUseCase(demoStrings = get())
    }

    single<MakeDemoAppCollectionUseCase> {
        MakeDemoAppCollectionUseCase(demoStringMaps = get())
    }

    single<MakeDemoAppGradeCollectionsUseCase> {
        MakeDemoAppGradeCollectionsUseCase(demoStrings = get())
    }

    single<MakeDemoAppLearningUnitManifestUseCase> {
        MakeDemoAppLearningUnitManifestUseCase(demoStrings = get())
    }

    single<MakeDemoAppLearningUnitTinCanXmlUseCase> {
        MakeDemoAppLearningUnitTinCanXmlUseCase(demoStrings = get())
    }

    /*
     * School scope: used as the basis for virtual hosting.
     */
    scope<SchoolDirectoryEntry> {
        fun Scope.schoolUrl(): Url = SchoolDirectoryEntryScopeId.parse(id).schoolUrl

        scoped<ServerAccountScopeManager> {
            ServerAccountScopeManager(
                schoolUrl = schoolUrl(),
                schoolScope = this,
            )
        }

        scoped<UsernameSuggestionUseCase> {
            UsernameSuggestionUseCaseServer(
                schoolDb = get(),
                filterUsernameUseCase = get(),
            )
        }
        scoped<VerifySignInWithPasskeyUseCase> {
            VerifySignInWithPasskeyUseCase(
                schoolDb = get(),
                json = get(),
                decodeUserHandleUseCase = get(),
            )
        }
        scoped<RespectSchoolPath> {
            val schoolDirName = schoolUrl().sanitizedForFilename()
            val schoolDirFile = File(dataDir, schoolDirName).also {
                if(!it.exists())
                    it.mkdirs()
            }

            RespectSchoolPath(
                path = Path(schoolDirFile.absolutePath)
            )
        }

        scoped<RespectSchoolDatabase> {
            val schoolPath: RespectSchoolPath = get()
            val appDb: RespectAppDatabase = get()
            val xxHasher: XXStringHasher = get()

            val schoolConfig = runBlocking {
                appDb.getSchoolConfigEntityDao().findByUid(xxHasher.hash(schoolUrl().toString()))
            } ?: throw IllegalStateException("School config not found for $id")

            val schoolConfigFile = File(schoolPath.path.toString())
            val dbFile = schoolConfigFile.resolve(schoolConfig.dbUrl)

            Room.databaseBuilder<RespectSchoolDatabase>(dbFile.absolutePath)
                .setDriver(BundledSQLiteDriver())
                .addCommonMigrations()
                .build()
        }

        scoped<ValidateAuthorizationUseCase> {
            ValidateAuthorizationUseCaseDbImpl(schoolDb = get())
        }

        scoped<GetTokenAndUserProfileWithCredentialUseCase> {
            GetTokenAndUserProfileWithCredentialDbImpl(
                schoolUrl = schoolUrl(),
                schoolDb = get(),
                xxHash = get(),
                verifyPasskeyUseCase = get(),
                respectAppDataSource = get(),
                authenticatePasswordUseCase = get(),
                authenticateQrBadgeUseCase  = get()
            )
        }

        scoped<AuthenticatePasswordUseCase> {
            AuthenticatePasswordUseCaseDbImpl(
                schoolDb = get(),
                encryptPersonPasswordUseCase = get(),
                uidNumberMapper = get(),
            )
        }

        scoped<AuthenticateQrBadgeUseCase> {
            AuthenticateQrBadgeUseCaseDbImpl(
                schoolDb = get(),
                uidNumberMapper = get(),
            )
        }

        scoped<GetActivePersonPasskeysUseCase> {
            GetActivePersonPasskeysDbImpl(
                schoolDb = get(),
                xxStringHasher = get(),
            )
        }

        scoped<RevokePasskeyUseCase> {
            RevokePersonPasskeyUseCaseDbImpl(
                schoolDb = get(),
                xxStringHasher = get(),
            )
        }

        scoped<SchoolPrimaryKeyGenerator> {
            SchoolPrimaryKeyGenerator(
                PrimaryKeyGenerator(SchoolPrimaryKeyGenerator.TABLE_IDS)
            )
        }

        scoped<GetInviteInfoUseCase> {
            GetInviteInfoUseCaseServer(
                schoolDb = get(),
                uidNumberMapper = get(),
            )
        }

        scoped<RedeemInviteUseCase> {
            val schoolScopeId = SchoolDirectoryEntryScopeId.parse(id)
            val accountScopeManager: ServerAccountScopeManager = get()

            RedeemInviteUseCaseDb(
                schoolDb = get(),
                schoolUrl = schoolScopeId.schoolUrl,
                schoolPrimaryKeyGenerator = get(),
                getTokenAndUserProfileUseCase = get(),
                schoolDataSource = { _, user ->
                    accountScopeManager.getOrCreateAccountScope(user).get()
                },
                uidNumberMapper = get(),
                json = get(),
                getPasskeyProviderInfoUseCase = get(),
                encryptPersonPasswordUseCase = get(),
                checkUsernameUniqueUseCase = get(),
            )
        }

        scoped<CreateInviteUseCase> {
            CreateInviteUseCaseDb(
                schoolDb = get(),
                uidNumberMapper = get(),
            )
        }
        scoped<CreateInviteLinkUseCase> {
            CreateInviteLinkUseCase(
                schoolUrl = schoolUrl(),
            )
        }

        scoped<AddDefaultSchoolPermissionGrantsUseCase>() {
            AddDefaultSchoolPermissionGrantsUseCase(
                schoolDb = get(),
                uidNumberMapper = get(),
            )
        }

        scoped<CheckUsernameUniqueUseCase> {
            CheckUsernameUniqueUseCaseServer(schoolDb = get())
        }
    }

    /*
     * AccountScope: as per the client, the Account Scope is linked to a parent School scope.
     *
     * All server-side dependencies in the account scope are "cheap" wrappers e.g. the
     * SchoolDataSource wrapper (which is tied to a specific account guid) is kept in the AccountScope,
     * but the RespectSchoolDatabase which has the actual DB connection is kept in the school scope.
     *
     * Dependencies in the account scope use factory so they are not retained in memory
     *
     * The account scope is created and then linked to the related school scope in the
     * authentication plugin in Application.kt. Scope creation and linking using factories must
     * be done in a way that is thread safe.
     */
    scope<RespectAccount> {
        factory<CheckPersonPermissionUseCase> {
            val accountScopeId = RespectAccountScopeId.parse(id)

            CheckPersonPermissionUseCaseDbImpl(
                authenticatedUser = accountScopeId.accountPrincipalId,
                schoolDb = get(),
                uidNumberMapper = get(),
            )
        }

        factory<SchoolDataSourceLocal> {
            val accountScopeId = RespectAccountScopeId.parse(id)

            SchoolDataSourceDb(
                schoolDb = get(),
                uidNumberMapper = get(),
                authenticatedUser = accountScopeId.accountPrincipalId,
                checkPersonPermissionUseCase = get(),
                json = get(),
                primaryKeyGenerator = get<SchoolPrimaryKeyGenerator>().primaryKeyGenerator,
                defaultAppCatalogUrl = RespectServerBuildConfig.RESPECT_DEFAULT_APPLIST,
                schoolUrl = accountScopeId.schoolUrl,
            )
        }

        factory<SchoolDataSource> {
            get<SchoolDataSourceLocal>()
        }

        factory<GetPermissionLastModifiedUseCase> {
            val accountScopeId = RespectAccountScopeId.parse(id)

            GetPermissionLastModifiedUseCaseDbImpl(
                schoolDb = get(),
                numberMapper = get(),
                authenticatedUser = accountScopeId.accountPrincipalId,
            )
        }

        factory<AddChildAccountUseCase> {
            val accountScopeId = RespectAccountScopeId.parse(id)

            AddChildAccountUseCaseDb(
                schoolPrimaryKeyGenerator = get(),
                authenticatedUser = accountScopeId.accountPrincipalId,
                schoolDataSource = get(),
            )
        }

        factory<UpdateClazzStudentXapiGroupUseCase> {
            val accountScopeId = RespectAccountScopeId.parse(id)

            UpdateClazzStudentXapiGroupUseCase(
                schoolDataSource = get(),
                authenticatedUserPrincipalId = accountScopeId.accountPrincipalId,
                schoolUrl = accountScopeId.schoolUrl,
            )
        }

        factory<SetSharedDevicePINUseCase> {
            SetSharedDevicePINUseCaseImpl(
                schoolDataSource = get(),
                respectAccountManager = get(),
                json = get(),
            )
        }
        factory<GetSharedDevicePINUseCase> {
            GetSharedDevicePINUseCaseImpl(
                schoolDataSource = get(),
                respectAccountManager = get(),
                json = get(),
                setSharedDevicePINUseCase = get()
            )
        }
    }
}