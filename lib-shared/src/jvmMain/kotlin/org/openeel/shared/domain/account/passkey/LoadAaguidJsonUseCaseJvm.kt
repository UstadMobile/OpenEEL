package world.respect.shared.domain.account.passkey

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import world.respect.credentials.passkey.model.AaguidProviderData

class LoadAaguidJsonUseCaseJvm(
    private val json: Json,
): LoadAaguidJsonUseCase {

    override suspend fun invoke(): AaguidProviderData? {
        return withContext(Dispatchers.IO) {
            this@LoadAaguidJsonUseCaseJvm::class.java.getResourceAsStream(
                "/aaguid.json"
            )?.bufferedReader(Charsets.UTF_8)?.use {
                it.readText()
            }?.let {
                json.decodeFromString(it)
            }
        }
    }

}