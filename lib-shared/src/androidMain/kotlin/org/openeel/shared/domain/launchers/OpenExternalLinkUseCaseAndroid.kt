package world.respect.shared.domain.launchers

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import world.respect.shared.domain.openexternallink.OpenExternalLinkUseCase

class OpenExternalLinkUseCaseAndroid(
    private val context: Context
) : OpenExternalLinkUseCase {

    override suspend fun invoke(request: OpenExternalLinkUseCase.OpenLinkRequest) {
        withContext(Dispatchers.Main) {
            try {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    request.url.toString().toUri(),
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(intent)

            } catch (e: ActivityNotFoundException) {
               print("No browser found to open web URL $e")
            }
        }
    }

}
