package world.respect.shared.domain.sendinvite

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import world.respect.shared.domain.sharelink.LaunchSendSmsUseCase
import world.respect.shared.domain.sharelink.LaunchSendSmsUseCase.Companion.EXTRA_SMS_BODY
import world.respect.shared.domain.sharelink.LaunchSendSmsUseCase.Companion.SMS_URI_SCHEME

class LaunchSendSmsAndroid(private val context: Context) : LaunchSendSmsUseCase {
    override suspend fun invoke(body: String) = withContext(Dispatchers.Main) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO, SMS_URI_SCHEME.toUri()).apply {
                putExtra(EXTRA_SMS_BODY, body)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Throwable) {
        }
    }
}
