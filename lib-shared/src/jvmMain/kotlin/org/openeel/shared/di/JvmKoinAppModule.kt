package world.respect.shared.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.koin.dsl.module
import world.respect.domain.getfavicons.GetFavIconUseCase
import world.respect.shared.domain.getfavicons.GetFavIconsUseCaseImpl
import world.respect.shared.domain.opds.validator.OpdsFeedValidator
import world.respect.shared.domain.opds.validator.OpdsPublicationValidator
import world.respect.shared.domain.opds.validator.ValidateLinkUseCaseImpl
import world.respect.shared.domain.opds.validator.ValidateOpdsPublicationUseCase
import world.respect.shared.domain.respectappmanifest.validator.RespectAppManifestValidator
import world.respect.shared.domain.validator.ValidateHttpResponseForUrlUseCase
import world.respect.domain.validator.ValidateLinkUseCase

val jvmKoinAppModule = module {
    single<Json> {
        Json {
            encodeDefaults = false
            ignoreUnknownKeys = true
        }
    }
    
    single<OkHttpClient> {
        OkHttpClient.Builder()
            .dispatcher(
                Dispatcher().also {
                    it.maxRequests = 30
                    it.maxRequestsPerHost = 10
                }
            ).build()
    }
    
    single<HttpClient> {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(json = get())
            }
            engine {
                preconfigured = get()
            }
        }
    }

    single<ValidateHttpResponseForUrlUseCase> {
        ValidateHttpResponseForUrlUseCase(
            httpClient = get()
        )
    }

    single<ValidateOpdsPublicationUseCase> {
        ValidateOpdsPublicationUseCase(
            validateHttpResponseForUrlUseCase = get()
        )
    }

    single<OpdsFeedValidator> {
        OpdsFeedValidator(
            json = get(),
            httpClient = get(),
            validateOpdsPublicationUseCase = get()
        )
    }

    single<OpdsPublicationValidator> {
        OpdsPublicationValidator(
            httpClient = get(),
            json = get(),
            validateOpdsPublicationUseCase = get()
        )
    }

    single<RespectAppManifestValidator> {
        RespectAppManifestValidator(
            json = get(),
            validateHttpResponseForUrlUseCase = get(),
            getFavIconUseCase = get(),
            httpClient = get(),
        )
    }

    single<ValidateLinkUseCase> {
        ValidateLinkUseCaseImpl(
            opdsFeedValidator = get(),
            opdsPublicationValidator = get(),
            respectAppManifestValidator = get(),
            validateHttpResponseForUrlUseCase = get(),
        )
    }

    single<GetFavIconUseCase> {
        GetFavIconsUseCaseImpl()
    }
    
    
}