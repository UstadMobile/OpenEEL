package world.respect.credentials.passkey

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.net.Uri
import android.os.Build
import androidx.core.net.toUri

// to check domain is verified
// https://developer.android.com/training/app-links/verify-android-applinks#user-prompt-domain-verification-manager
//
class VerifyDomainUseCaseImpl(
    private val context: Context
) : VerifyDomainUseCase {

    @SuppressLint("QueryPermissionsNeeded")
    override suspend fun invoke(rpId: String): Boolean {
        return if(Build.VERSION.SDK_INT >= 31) {
            val manager = context.getSystemService(DomainVerificationManager::class.java)


            val userState = manager.getDomainVerificationUserState(context.packageName)
                ?: return false

            val verifiedDomains = userState.hostToStateMap
                .filterValues { it == DomainVerificationUserState.DOMAIN_STATE_VERIFIED }

            verifiedDomains.keys.any { domain ->
                rpId.contains(domain)
            }
        }else {
            return try {
                val uri = if (rpId.startsWith("http", ignoreCase = true)) rpId.toUri()
                else "https://$rpId/".toUri()

                val intent = Intent(Intent.ACTION_VIEW, uri)

                val resolves = context.packageManager.queryIntentActivities(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                resolves.any { it.activityInfo?.packageName == context.packageName }
            } catch (t: Throwable) {
                false
            }
        }
    }

}
