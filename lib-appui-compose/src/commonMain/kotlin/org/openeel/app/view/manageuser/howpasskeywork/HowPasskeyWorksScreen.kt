package world.respect.app.view.manageuser.howpasskeywork

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultItemPadding
import world.respect.shared.generated.resources.*
import world.respect.shared.viewmodel.manageuser.howpasskeywork.HowPasskeyWorksViewModel

@Composable
fun HowPasskeyWorksScreen(
    viewModel: HowPasskeyWorksViewModel
) {
    HowPasskeyWorksScreen()
}

@Composable
fun HowPasskeyWorksScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.passkey_title),
            modifier = Modifier.defaultItemPadding()

        )

        ListItem(
            headlineContent = { Text(text = stringResource(Res.string.passkey_q1)) },
            supportingContent = {
                Text(
                    text = stringResource(Res.string.passkey_a1),
                )
            }
        )


        ListItem(
            headlineContent = { Text(text = stringResource(Res.string.passkey_q2)) },
            supportingContent = {
                Text(
                    text = stringResource(Res.string.passkey_a2),
                )
            }
        )


        ListItem(
            headlineContent = { Text(text = stringResource(Res.string.passkey_q3)) },
            supportingContent = {
                Text(
                    text = stringResource(Res.string.passkey_a3),
                )
            }
        )
    }
}
