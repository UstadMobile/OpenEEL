package org.openeel.app.view.manageuser.learningspace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import org.openeel.shared.viewmodel.manageuser.learningspace.CreateLearningSpaceUiState
import org.openeel.shared.viewmodel.manageuser.learningspace.CreateLearningSpaceViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.add_from_link

@Composable
fun CreateLearningSpaceScreen(
    viewModel: CreateLearningSpaceViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    CreateLearningSpaceScreen(
        uiState = uiState,
        onClickAddLink = { viewModel.onClickAddLink() },

        )
}

@Composable
fun CreateLearningSpaceScreen(
    uiState: CreateLearningSpaceUiState,
    onClickAddLink: () -> Unit,

    ) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickAddLink() },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = null,
            )
        },
        headlineContent = {
            Text(
                text = stringResource(Res.string.add_from_link),
            )
        }
    )

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {  },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = null,
            )
        },
        headlineContent = {
            Text(
                text = stringResource(Res.string.add_from_link),
            )
        }
    )

}
