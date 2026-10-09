package world.respect.shared.domain.launchers

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.net.toUri

class LaunchSendWhatsAppUseCaseAndroid(
    private val context: Context
) : LaunchSendWhatsAppUseCase {

    override suspend fun invoke(phoneNumber: String) {
        withContext(Dispatchers.Main) {
            try {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    "$WHATSAPP_URL$phoneNumber".toUri()
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                print("WhatsApp not installed + $e")
            }
        }
    }

    companion object{
        const val WHATSAPP_URL = "https://wa.me/"
    }
}


