package world.respect.app.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import world.respect.libutil.util.selectLang

/**
 * A TextField that supports LangMaps
 *
 */
@Composable
fun LangMapTextField(
    value: Map<String, String>,
    onValueChange: (Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
    supportingText: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
) {
    val currentLocale = LocalAppLocale.current

    var dropdownExpanded: Boolean by remember {
        mutableStateOf(false)
    }

    val valueMap = value.ifEmpty {
        mapOf(currentLocale to "")
    }

    fun doLangSelect() = selectLang(
        preferredLocales = listOf(currentLocale),
        availableLocales = valueMap.keys.toList(),
    ).replace("_", "-")

    var editingLocale by remember {
        mutableStateOf(doLangSelect())
    }

    LaunchedEffect(editingLocale, value){
        if(!value.containsKey(editingLocale)) {
            editingLocale = doLangSelect()
        }
    }

    OutlinedTextField(
        modifier = modifier,
        value = value[editingLocale] ?: "",
        label = label,
        enabled = enabled,
        onValueChange = { newText ->
            onValueChange(
                value.toMutableMap().apply {
                    put(editingLocale, newText)
                }.toMap()
            )
        },
        supportingText = supportingText,
        trailingIcon = {
            IconButton(
                onClick = {
                    dropdownExpanded = !dropdownExpanded
                }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                )
            }

            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = {
                    dropdownExpanded = false
                }
            ){
                value.keys.forEach { langOption ->
                    DropdownMenuItem(
                        text = { Text(langOption) },
                        onClick = {
                            editingLocale = langOption
                        }
                    )
                }
            }
        }
    )
}


@Composable
@Preview
fun LangMapEditFieldPreview() {
    CompositionLocalProvider(LocalAppLocale provides customAppLocale) {

        var langMapValue by remember {
            mutableStateOf(emptyMap<String, String>())
        }

        LangMapTextField(
            modifier = Modifier.defaultItemPadding().fillMaxWidth(),
            value = langMapValue,
            label = {
                Text("Title")
            },
            onValueChange = {
                langMapValue = it
            },
        )
    }
}