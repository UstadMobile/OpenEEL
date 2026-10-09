package world.respect.app.view.manageuser.sharefeedback

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import world.respect.shared.viewmodel.manageuser.sharefeedback.ShareFeedbackViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Whatsapp
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.email_respect
import world.respect.shared.generated.resources.public_forum
import world.respect.shared.generated.resources.whatsapp_respect

@Composable
fun ShareFeedbackScreen(
    viewModel: ShareFeedbackViewModel
) {
    ShareFeedbackScreen(
        onClickWhatsApp = viewModel::onClickWhatsApp,
        onClickEmail = viewModel::onClickEmail,
        onClickPublicForum = viewModel::onClickPublicForum
    )
}

@Composable
fun ShareFeedbackScreen(
    onClickWhatsApp: () -> Unit,
    onClickEmail: () -> Unit,
    onClickPublicForum: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ) {

        ListItem(
            modifier = Modifier.clickable {
                onClickWhatsApp()
            },
            headlineContent = { Text(stringResource(Res.string.whatsapp_respect)) },
            leadingContent = {
                Icon(imageVector = Icons.Outlined.Whatsapp, contentDescription = null)
            }
        )

        ListItem(
            modifier = Modifier.clickable {
                onClickEmail()
            },
            headlineContent = { Text(stringResource(Res.string.email_respect)) },
            leadingContent = {
                Icon(imageVector = Icons.Outlined.Email, contentDescription = null)
            }
        )

        ListItem(
            modifier = Modifier.clickable {
                onClickPublicForum()
            },
            headlineContent = { Text(stringResource(Res.string.public_forum)) },
            leadingContent = {
                Icon(imageVector = Icons.Outlined.People, contentDescription = null)
            }
        )
    }
}
