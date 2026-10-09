package world.respect.app.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.getKoin
import world.respect.lib.opds.model.ReadiumLink
import world.respect.shared.domain.getlanguageendonym.GetLanguageEndonymUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.also_available_in
import world.respect.shared.generated.resources.show_less
import world.respect.shared.generated.resources.show_more

const val ALT_LANGS_DEFAULT_MAX_ITEMS = 10

@Composable
fun AlternativeLangLinks(
    altLangLinks: List<ReadiumLink>,
    onClickAlternativeLangVersion: (ReadiumLink) -> Unit,
    modifier: Modifier = Modifier,
    defaultMaxItems: Int = ALT_LANGS_DEFAULT_MAX_ITEMS,
) {

    val koin = getKoin()
    val getLanguageEndonymUseCase : GetLanguageEndonymUseCase = remember {
        koin.get()
    }

    var showAll by remember {
        mutableStateOf(false)
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
            altLangLinks.let {
                if(showAll) it else it.take(defaultMaxItems)
            }.forEach { langLink ->
                AssistChip(
                    onClick = {
                        onClickAlternativeLangVersion(langLink)
                    },
                    label = {
                        Text(
                            langLink.language?.firstOrNull()?.let { langCode ->
                                getLanguageEndonymUseCase(langCode)
                            }?.let { stringResource(it) } ?: langLink.language?.firstOrNull() ?: ""
                        )
                    }
                )
            }
        }

        if(altLangLinks.size > defaultMaxItems) {
            TextButton(
                onClick = { showAll = !showAll },
            ) {
                Text(
                    stringResource(
                        if(showAll) {
                            Res.string.show_less
                        }else {
                            Res.string.show_more
                        }
                    )
                )
            }
        }
    }

}