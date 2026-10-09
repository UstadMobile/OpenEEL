package world.respect.shared.domain.sendinvite

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import world.respect.shared.domain.sharelink.LaunchSendEmailUseCase

class LaunchSendEmailAndroid(
    private val context: Context
) : LaunchSendEmailUseCase {

    override suspend fun invoke(
        request: LaunchSendEmailUseCase.LaunchSendEmailRequest
    ) {
        withContext(Dispatchers.Main) {
            val builder = Uri.Builder()
                .scheme("mailto")

            request.to?.also {
                builder.opaquePart(request.to)
            }

            request.subject?.also {
                builder.appendQueryParameter("subject", it)
            }

            request.body?.also {
                builder.appendQueryParameter("body", it)
            }

            val intent = Intent(Intent.ACTION_SENDTO, builder.build())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Log.w("LaunchSendEmailAndroid", "No email app installed")
                throw e
            }
        }
    }

}
