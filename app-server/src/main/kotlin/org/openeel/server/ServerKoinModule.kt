package org.openeel.server
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
import org.openeel.credentials.passkey.request.DecodeUserHandleUseCase
import org.openeel.credentials.passkey.request.GetPasskeyProviderInfoUseCase
import org.openeel.datalayer.SchoolDirectoryDataSource
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.SchoolDataSourceLocal
import org.openeel.datalayer.SchoolDirectoryDataSourceLocal
import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.APP_MIGRATION_8_9_SERVER
import org.openeel.datalayer.db.SchoolDirectoryDataSourceDb
import org.openeel.datalayer.db.RespectAppDatabase
import org.openeel.datalayer.db.SchoolDatabase
import org.openeel.datalayer.db.SchoolDataSourceDb
import org.openeel.datalayer.db.addCommonMigrations
import org.openeel.datalayer.db.school.domain.AddDefaultSchoolPermissionGrantsUseCase
import org.openeel.datalayer.db.school.domain.CheckPersonPermissionUseCaseDbImpl
import org.openeel.datalayer.db.school.domain.GetPermissionLastModifiedUseCaseDbImpl
import org.openeel.datalayer.db.schooldirectory.SchoolDirectoryResourceDb
import org.openeel.datalayer.respect.model.SchoolDirectoryEntry
import org.openeel.datalayer.school.domain.CheckPersonPermissionUseCase
import org.openeel.datalayer.school.domain.GetPermissionLastModifiedUseCase
import org.openeel.datalayer.schooldirectory.SchoolDirectoryResourceLocal
import org.openeel.datalayer.shared.XXHashUidNumberMapper
import org.openeel.lib.primarykeygen.PrimaryKeyGenerator
import org.openeel.libutil.ext.sanitizedForFilename
import org.openeel.libxxhash.XXStringHasher
import org.openeel.libxxhash.jvmimpl.XXStringHasherCommonJvm
import org.openeel.server.account.invite.GetInviteInfoUseCaseServer
import org.openeel.server.account.invite.username.UsernameSuggestionUseCaseServer
import org.openeel.server.account.invite.username.checkusernameunique.CheckUsernameUniqueUseCaseServer
import org.openeel.shared.domain.account.passkey.VerifySignInWithPasskeyUseCase
import org.openeel.server.domain.school.add.AddSchoolUseCase
import org.openeel.server.domain.school.add.AddServerManagedDirectoryCallback
import org.openeel.server.domain.school.add.RegisterSchoolUseCaseImpl
import org.openeel.server.domain.school.demoapp.DemoStringMaps
import org.openeel.server.domain.school.demoapp.MakeDemoAppCollectionUseCase
import org.openeel.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase
import org.openeel.server.domain.school.demoapp.MakeDemoAppLearningUnitManifestUseCase
import org.openeel.server.domain.school.demoapp.MakeDemoAppManifestUseCase
import org.openeel.server.domain.school.demoapp.MakeDemoAppLearningUnitTinCanXmlUseCase
import org.openeel.server.domain.school.verify.VerifySchoolUrlPointsToThisServerUseCase
import org.openeel.server.util.SchoolUrlVerificationManager
import org.openeel.shared.domain.account.UserAccount
import org.openeel.shared.domain.account.authenticatepassword.AuthenticatePasswordUseCase
import org.openeel.shared.domain.account.authenticatepassword.AuthenticateQrBadgeUseCase
import org.openeel.shared.domain.account.authwithpassword.GetTokenAndUserProfileWithCredentialDbImpl
import org.openeel.shared.domain.account.child.AddChildAccountUseCase
import org.openeel.shared.domain.account.child.AddChildAccountUseCaseDb
import org.openeel.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithCredentialUseCase
import org.openeel.shared.domain.account.invite.CreateInviteUseCase
import org.openeel.shared.domain.account.invite.CreateInviteUseCaseDb
import org.openeel.shared.domain.account.invite.GetInviteInfoUseCase
import org.openeel.shared.domain.account.invite.RedeemInviteUseCase
import org.openeel.shared.domain.account.invite.RedeemInviteUseCaseDb
import org.openeel.shared.domain.account.passkey.DecodeUserHandleUseCaseImpl
import org.openeel.shared.domain.account.passkey.GetPasskeyProviderInfoUseCaseImpl
import org.openeel.shared.domain.account.passkey.GetActivePersonPasskeysDbImpl
import org.openeel.shared.domain.account.passkey.GetActivePersonPasskeysUseCase
import org.openeel.shared.domain.account.passkey.LoadAaguidJsonUseCase
import org.openeel.shared.domain.account.passkey.LoadAaguidJsonUseCaseJvm
import org.openeel.shared.domain.account.passkey.RevokePasskeyUseCase
import org.openeel.shared.domain.account.passkey.RevokePersonPasskeyUseCaseDbImpl
import org.openeel.shared.domain.account.setpassword.EncryptPersonPasswordUseCase
import org.openeel.shared.domain.account.setpassword.EncryptPersonPasswordUseCaseImpl
import org.openeel.shared.domain.account.username.UsernameSuggestionUseCase
import org.openeel.shared.domain.account.username.checkusernameunique.CheckUsernameUniqueUseCase
import org.openeel.shared.domain.account.username.filterusername.FilterUsernameUseCase
import org.openeel.shared.domain.account.validateauth.ValidateAuthorizationUseCase
import org.openeel.shared.domain.account.validateauth.ValidateAuthorizationUseCaseDbImpl
import org.openeel.shared.domain.createlink.CreateInviteLinkUseCase
import org.openeel.shared.domain.enrollments.UpdateClazzStudentXapiGroupUseCase
import org.openeel.shared.domain.navigation.deeplink.UrlToCustomDeepLinkUseCase
import org.openeel.shared.domain.school.SchoolPath
import org.openeel.shared.domain.school.SchoolPrimaryKeyGenerator
import org.openeel.shared.domain.school.add.RegisterSchoolUseCase
import org.openeel.shared.util.di.UserAccountScopeId
import org.openeel.shared.util.di.SchoolDirectoryEntryScopeId
import org.openeel.sharedse.domain.account.authenticatepassword.AuthenticatePasswordUseCaseDbImpl
import org.openeel.sharedse.domain.account.authenticatepassword.AuthenticateQrBadgeUseCaseDbImpl
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

    single<SchoolDirectoryResourceLocal> {
        SchoolDirectoryResourceDb(
            respectAppDb = get(),
            xxStringHasher = get()
        )
    }

    single<SchoolDirectoryDataSourceLocal> {
        SchoolDirectoryDataSourceDb(
            respectAppDatabase = get(),
            json = get(),
            xxStringHasher = get(),
        )
    }

    single<SchoolDirectoryDataSource> {
        get<SchoolDirectoryDataSourceLocal>()
    }

    single<FilterUsernameUseCase> {
        FilterUsernameUseCase()
    }

    single<AddSchoolUseCase> {
        AddSchoolUseCase(
            directoryDataSource = get< SchoolDirectoryDataSourceLocal>().schoolDirectoryResource,
            schoolDirectoryEntryDataSource = get<SchoolDirectoryDataSourceLocal>().schoolDirectoryEntryResource,
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
        scoped<SchoolPath> {
            val schoolDirName = schoolUrl().sanitizedForFilename()
            val schoolDirFile = File(dataDir, schoolDirName).also {
                if(!it.exists())
                    it.mkdirs()
            }

            SchoolPath(
                path = Path(schoolDirFile.absolutePath)
            )
        }

        scoped<SchoolDatabase> {
            val schoolPath: SchoolPath = get()
            val appDb: RespectAppDatabase = get()
            val xxHasher: XXStringHasher = get()

            val schoolConfig = runBlocking {
                appDb.getSchoolConfigEntityDao().findByUid(xxHasher.hash(schoolUrl().toString()))
            } ?: throw IllegalStateException("School config not found for $id")

            val schoolConfigFile = File(schoolPath.path.toString())
            val dbFile = schoolConfigFile.resolve(schoolConfig.dbUrl)

            Room.databaseBuilder<SchoolDatabase>(dbFile.absolutePath)
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
                schoolDirectoryDataSource = get(),
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
    scope<UserAccount> {
        factory<CheckPersonPermissionUseCase> {
            val accountScopeId = UserAccountScopeId.parse(id)

            CheckPersonPermissionUseCaseDbImpl(
                authenticatedUser = accountScopeId.accountPrincipalId,
                schoolDb = get(),
                uidNumberMapper = get(),
            )
        }

        factory<SchoolDataSourceLocal> {
            val accountScopeId = UserAccountScopeId.parse(id)

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
            val accountScopeId = UserAccountScopeId.parse(id)

            GetPermissionLastModifiedUseCaseDbImpl(
                schoolDb = get(),
                numberMapper = get(),
                authenticatedUser = accountScopeId.accountPrincipalId,
            )
        }

        factory<AddChildAccountUseCase> {
            val accountScopeId = UserAccountScopeId.parse(id)

            AddChildAccountUseCaseDb(
                schoolPrimaryKeyGenerator = get(),
                authenticatedUser = accountScopeId.accountPrincipalId,
                schoolDataSource = get(),
            )
        }

        factory<UpdateClazzStudentXapiGroupUseCase> {
            val accountScopeId = UserAccountScopeId.parse(id)

            UpdateClazzStudentXapiGroupUseCase(
                schoolDataSource = get(),
                authenticatedUserPrincipalId = accountScopeId.accountPrincipalId,
                schoolUrl = accountScopeId.schoolUrl,
            )
        }

    }


}