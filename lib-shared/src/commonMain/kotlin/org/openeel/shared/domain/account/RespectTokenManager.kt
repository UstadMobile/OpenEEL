package world.respect.shared.domain.account

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.serialization.json.Json
import world.respect.datalayer.school.model.AuthToken
import world.respect.datalayer.AuthTokenProvider

/**
 * On Android this could move to using:
 * https://developer.android.com/privacy-and-security/keystore
 */
class RespectTokenManager(
    private val settings: Settings,
    private val json: Json,
) {

    private val tokenFlow = MutableStateFlow<Map<String, AuthToken>>(
        settings.getStringOrNull(SETTINGS_KEY)?.let {
            json.decodeFromString(it)
        } ?: emptyMap()
    )

    inner class AuthTokenProviderImpl(private val accountId: String): AuthTokenProvider {

        override fun provideToken(): AuthToken {
            return tokenFlow.value[accountId]
                ?: throw IllegalStateException("No token available")
        }
    }

    fun providerFor(accountKey: String): AuthTokenProvider {
        return AuthTokenProviderImpl(accountKey)
    }

    /**
     * @param accountKey string - should be in the form of user@server
     */
    fun storeToken(accountKey: String, token: AuthToken) {
        val newTokenMap = tokenFlow.updateAndGet { prev ->
            prev.toMutableMap().apply {
                put(accountKey, token)
            }
        }

        settings.putString(SETTINGS_KEY, json.encodeToString(newTokenMap))
    }

    fun removeToken(accountKey: String) {
        val newTokenMap = tokenFlow.updateAndGet { prev ->
            prev.toMutableMap().apply {
                remove(accountKey)
            }
        }

        settings.putString(SETTINGS_KEY, json.encodeToString(newTokenMap))
    }

    companion object {

        const val SETTINGS_KEY = "token-manager-"

    }


}