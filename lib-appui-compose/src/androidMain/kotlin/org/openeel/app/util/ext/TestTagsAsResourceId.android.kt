package org.openeel.app.util.ext

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId as semanticsTestTagsAsResourceId

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.testTagsAsResourceId(): Modifier = semantics {
    semanticsTestTagsAsResourceId = true
}
