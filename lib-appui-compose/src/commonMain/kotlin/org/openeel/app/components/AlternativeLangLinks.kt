package org.openeel.app.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.getKoin
import org.openeel.lib.opds.model.ReadiumLink
import org.openeel.shared.domain.getlanguageendonym.GetLanguageEndonymUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.also_available_in

@Composable
fun AlternativeLangLinks(
    altLangLinks: List<ReadiumLink>,
    onClickAlternativeLangVersion: (ReadiumLink) -> Unit,
    modifier: Modifier = Modifier,
) {

    val koin = getKoin()
    val getLanguageEndonymUseCase : GetLanguageEndonymUseCase = remember {
        koin.get()
    }

    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(vertical = 8.dp),
            text = stringResource(Res.string.also_available_in),
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            altLangLinks.forEach { langLink ->
                AssistChip(
                    onClick = {
                        onClickAlternativeLangVersion(langLink)
                    },
                    label = {
                        Text(
                            langLink.language?.firstOrNull()?.let { langCode ->
                                getLanguageEndonymUseCase(langCode)
                            }?.let { stringResource(it) } ?: ""
                        )
                    }
                )
            }
        }
    }

}