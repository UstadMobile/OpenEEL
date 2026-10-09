package world.respect.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.how_passkey_works
import world.respect.shared.generated.resources.sign_in_faster
import world.respect.shared.generated.resources.sign_in_faster_description
import world.respect.shared.generated.resources.sign_up_with_passkey
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText

@Composable
fun RespectPasskeySignInFasterCard(
    onClickPasskeySignup: () -> Unit,
    onClickHowPasskeysWork: () -> Unit,
    modifier: Modifier = Modifier,
    buttonText: UiText = Res.string.sign_up_with_passkey.asUiText(),
    generalError: UiText? = null,
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.sign_in_faster),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = buildAnnotatedString {
                    append(stringResource(Res.string.sign_in_faster_description))
                    append(" ")
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(stringResource(Res.string.how_passkey_works))
                    }
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                ),
                modifier = Modifier.clickable { onClickHowPasskeysWork() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onClickPasskeySignup,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(uiTextStringResource(buttonText))
            }

            generalError?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(uiTextStringResource(it))
            }
        }
    }
}