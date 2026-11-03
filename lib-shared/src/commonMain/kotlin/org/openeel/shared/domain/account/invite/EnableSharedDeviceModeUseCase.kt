package org.openeel.shared.domain.account.invite

import com.russhwolf.settings.Settings
import io.ktor.http.Url
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import org.openeel.shared.domain.account.isSharedDeviceSessionWithoutProfile
import org.openeel.datalayer.school.model.Person
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.util.ext.isSameAccount
import kotlin.time.Duration.Companion.seconds

class EnableSharedDeviceModeUseCase(
    private val accountManager: AppAccountManager,
    private val settings: Settings,
) {
    suspend operator fun invoke(
        redeemInviteRequest: RedeemInviteRequest,
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

            withTimeout(ENABLE_TIMEOUT.seconds) {
                accountManager.selectedAccountAndPersonFlow.first { selected ->
                    selected != null &&
                        selected.session.account.userGuid == personRegistered.guid &&
                        selected.session.isSharedDeviceSessionWithoutProfile(selected.person)
                }
            }

            settings.putString(CURRENT_DEVICE_GUID, personRegistered.guid)

            return personRegistered

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    companion object {
        const val CURRENT_DEVICE_GUID = "current_device_guid"
        private const val ENABLE_TIMEOUT = 30L
    }
}
