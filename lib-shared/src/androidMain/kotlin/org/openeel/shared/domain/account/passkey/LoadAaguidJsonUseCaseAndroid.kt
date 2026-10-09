package world.respect.shared.domain.account.passkey

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import world.respect.credentials.passkey.model.AaguidProviderData

class LoadAaguidJsonUseCaseAndroid(
    private val appContext: Context,
    private val json: Json,
) : LoadAaguidJsonUseCase{

    override suspend fun invoke(): AaguidProviderData? {
        return withContext(Dispatchers.IO) {
            json.decodeFromString(
                appContext.assets.open("aaguid.json").bufferedReader(Charsets.UTF_8).use {
                    it.readText()
                }
            )
        }

    }

}