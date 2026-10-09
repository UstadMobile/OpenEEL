package world.respect.credentials.passkey

import android.annotation.SuppressLint
import android.util.Log
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialException
import io.ktor.http.Url
import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.json.Json
import world.respect.credentials.passkey.model.AuthenticationResponseJSON
import world.respect.credentials.passkey.request.CreatePublicKeyCredentialCreationOptionsJsonUseCase
import world.respect.datalayer.UidNumberMapper
import world.respect.credentials.passkey.request.GetPasskeyProviderInfoUseCase

/**
 * Create a passkey on Android. This will show a bottom sheet for the user to approve creating a new
 * passkey.
 *
 * The App's DI is at the Application context level, however passkeys require an activity context.
 * This UseCase provides a channel that must be collected by the activity (eg MainActivity) using
 * CreatePasskeyUseCaseProcessor.
 *
 * See https://developer.android.com/identity/sign-in/credential-manager#create-passkey
 */
class CreatePasskeyUseCaseAndroidImpl(
    private val sender: CreatePasskeyUseCaseAndroidChannelHost,
    private val json: Json,
    private val createPublicKeyJsonUseCase: CreatePublicKeyCredentialCreationOptionsJsonUseCase,
    private val schoolUrl: Url,
    private val uidNumberMapper: UidNumberMapper,
    private val getPasskeyProviderInfoUseCase: GetPasskeyProviderInfoUseCase,
) : CreatePasskeyUseCase {

    data class CreatePublicKeyCredentialRequestJob(
        val request: CreatePublicKeyCredentialRequest,
        val response: CompletableDeferred<CreatePublicKeyCredentialResponse> = CompletableDeferred()
    )


    /**
     * @throws CreateCredentialException if CredentialManager throws an exception
     *
     * SuppressLint note: This will not be invoked when CheckPasskeySupportUseCase returns false,
     * which it will if minimum Android version requirement is not met.
     */
    @SuppressLint("PublicKeyCredential")
    override suspend fun invoke(
        request: CreatePasskeyUseCase.Request
    ): CreatePasskeyUseCase.CreatePasskeyResult {
        val personUidNumVal = uidNumberMapper(request.personUid)
        return try {
            val job = CreatePublicKeyCredentialRequestJob(
                request = CreatePublicKeyCredentialRequest(
                    requestJson = json.encodeToString(
                        createPublicKeyJsonUseCase(
                            CreatePublicKeyCredentialCreationOptionsJsonUseCase.Request(
                                username = request.username,
                                rpId = request.rpId,
                                personUidNum = personUidNumVal,
                            )
                        )
                    ),
                    preferImmediatelyAvailableCredentials = false,
                ),
            )
            sender.send(job)
            val response = job.response.await()

            Log.d ( "passkey response:", response.registrationResponseJson)
            val passkeyResponse = json.decodeFromString<AuthenticationResponseJSON>(
                response.registrationResponseJson
            )
            val passkeyProviderInfo = getPasskeyProviderInfoUseCase(
                authenticatorData = passkeyResponse.response.authenticatorData
            )
            CreatePasskeyUseCase.PasskeyCreatedResult(
                passkeyResponse,
                RespectUserHandle(
                    personUidNum = personUidNumVal,
                    schoolUrl = schoolUrl,
                ),
                passkeyProviderInfo= passkeyProviderInfo
            )
        } catch (_: CreateCredentialCancellationException) {
            CreatePasskeyUseCase.UserCanceledResult()
        } catch (e: CreateCredentialException) {
            // See https://codelabs.developers.google.com/credential-manager-api-for-android#1
            Log.e(
                 "CreatePassKeyUseCase",  e.message, e,
            )
            CreatePasskeyUseCase.Error(e.message)
        }
    }
}