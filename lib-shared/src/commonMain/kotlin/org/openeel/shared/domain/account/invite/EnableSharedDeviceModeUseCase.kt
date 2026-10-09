package world.respect.shared.domain.account.invite

import com.russhwolf.settings.Settings
import io.ktor.http.Url
import world.respect.datalayer.school.model.Person
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.util.ext.isSameAccount

class EnableSharedDeviceModeUseCase(
    private val accountManager: RespectAccountManager,
    private val settings: Settings,
) {
    suspend operator fun invoke(
        redeemInviteRequest: RespectRedeemInviteRequest,
        schoolUrl: Url,
        useActiveUserAuth: Boolean = false
    ): Person {
        try {
            val personRegistered = accountManager.register(
                redeemInviteRequest = redeemInviteRequest,
                schoolUrl = schoolUrl,
                useActiveUserAuth = useActiveUserAuth
            )

            val deviceAccount = accountManager.activeAccount
                ?: throw IllegalStateException("Device account was not set as active")

            val currentAccounts = accountManager.accounts.value
            currentAccounts.forEach { account ->
                if (!account.isSameAccount(deviceAccount)) {
                    accountManager.removeAccount(account)
                }
            }

            settings.putBoolean(SETTINGS_KEY_IS_SHARED_MODE, true)

            return personRegistered

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    companion object {
        const val SETTINGS_KEY_IS_SHARED_MODE = "is_shared_device_mode"
    }
}