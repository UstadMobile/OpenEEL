package org.openeel.shared.domain.account.shareddevice.enableshareddevicemode

import com.russhwolf.settings.Settings
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import org.openeel.datalayer.school.model.Person
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.account.invite.RedeemInviteRequest
import org.openeel.shared.util.ext.isSameAccount
import kotlin.time.Duration.Companion.seconds

class EnableSharedDeviceModeUseCase(
    private val accountManager: AppAccountManager,
    private val settings: Settings,
    private val schoolUrl: Url,
) {

    suspend operator fun invoke(
        redeemInviteRequest: RedeemInviteRequest,
        useActiveUserAuth: Boolean = false
    ): Person {
        try {
            val personRegistered = accountManager.register(
                redeemInviteRequest = redeemInviteRequest,
                schoolUrl = schoolUrl,
                useActiveUserAuth = useActiveUserAuth
            )
            settings.putString(CURRENT_DEVICE_GUID, personRegistered.guid)

            val deviceAccount = accountManager.activeAccount
                ?: throw IllegalStateException("Device account was not set as active")

            val currentAccounts = accountManager.accounts.value
            currentAccounts.filterNot { it.isSameAccount(deviceAccount) }
                .forEach { accountManager.removeAccount(it) }

            withTimeout(ENABLE_TIMEOUT.seconds) {
                accountManager.selectedAccountAndPersonFlow.first { selected ->
                    selected != null &&
                        selected.session.account.userGuid == personRegistered.guid &&
                        selected.isSharedDeviceSessionWithoutStudentSelected
                }
            }

            return personRegistered
        } catch (e: Exception) {
            Napier.e(
                tag = "EnableSharedDeviceModeUseCase",
                message = "EnableSharedDeviceModeUseCase: exception",
                throwable = e
            )
            throw e
        }
    }

    companion object {
        const val CURRENT_DEVICE_GUID = "current_device_guid"
        private const val ENABLE_TIMEOUT = 30L
    }
}
