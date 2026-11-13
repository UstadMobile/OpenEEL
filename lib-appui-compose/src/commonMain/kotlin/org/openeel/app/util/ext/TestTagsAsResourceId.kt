package org.openeel.app.util.ext

import androidx.compose.ui.Modifier

/**
 * Exposes testTags as resource ids (on Android) so they can be found by Maestro using id. Needed
 * on content shown in a separate window (e.g. ModalBottomSheet, Dialog) because these do not
 * inherit the setting from the activity root.
 */
expect fun Modifier.testTagsAsResourceId(): Modifier
