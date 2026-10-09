package world.respect.credentials.passkey

import android.util.Log
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetPasswordOption
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.json.Json
import world.respect.credentials.passkey.model.AuthenticationResponseJSON
import world.respect.credentials.passkey.request.CreatePublicKeyCredentialRequestOptionsJsonUseCase

class GetCredentialUseCaseAndroidImpl(
    private val createPublicKeyCredentialRequestOptionsJsonUseCase: CreatePublicKeyCredentialRequestOptionsJsonUseCase,
    private val json: Json,
) : GetCredentialUseCase {

    data class GetCredentialJob(
        val request: GetCredentialRequest,
        val response: CompletableDeferred<GetCredentialResponse> = CompletableDeferred()
    )

    val requestChannel = Channel<GetCredentialJob>(capacity = Channel.RENDEZVOUS)

    override suspend fun invoke(rpId: String): GetCredentialUseCase.CredentialResult {
        val getPasswordOption = GetPasswordOption()
        val getPublicKeyCredentialOption = GetPublicKeyCredentialOption(
            requestJson = json.encodeToString(createPublicKeyCredentialRequestOptionsJsonUseCase(rpId=rpId))
        )

        //As per https://developer.android.com/identity/sign-in/credential-manager#sign-in when
        // preferImmediatelyAvailableCredentials = true then the dialog will only be shown if the
        // user has accounts that they can select.
        val requestCredentialJob = GetCredentialJob(
            request = GetCredentialRequest(
                credentialOptions = listOf(getPasswordOption, getPublicKeyCredentialOption),
                preferImmediatelyAvailableCredentials = true
            )
        )

        requestChannel.send(requestCredentialJob)

        return try {
            val result = requestCredentialJob.response.await()

            //As per https://developer.android.com/identity/sign-in/credential-manager#sign-in
            when (val credential = result.credential) {
                is PasswordCredential -> {
                    GetCredentialUseCase.PasswordCredentialResult(
                        credentialUsername = credential.id,
                        password = credential.password
                    )
                }

                is PublicKeyCredential -> {
                    val authResponseJson = credential.authenticationResponseJson
                    Log.d ("passkey response" , authResponseJson)
                    val parsedResponse = json.decodeFromString<AuthenticationResponseJSON>(authResponseJson)

                    GetCredentialUseCase.PasskeyCredentialResult(
                       parsedResponse
                    )
                }

                else -> {
                    GetCredentialUseCase.Error("Unknown credential type.")
                }
            }
        } catch (_: NoCredentialException) {
            GetCredentialUseCase.NoCredentialAvailableResult()
        }
        catch (_: GetCredentialCancellationException) {
            GetCredentialUseCase.UserCanceledResult()
        }catch (e: GetCredentialException) {
            GetCredentialUseCase.Error("Failed to get credential: ${e.message}")
        }
    }
}