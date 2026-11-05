package org.openeel.app.view.manageuser.getstarted

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.components.defaultItemPadding
import org.openeel.app.generated.resources.Res as AppRes
import org.openeel.app.generated.resources.connect_space
import org.openeel.app.generated.resources.create_space
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.connect_existing_learning_space
import org.openeel.shared.generated.resources.connect_existing_learning_space_description
import org.openeel.shared.generated.resources.create_new_learning_space
import org.openeel.shared.generated.resources.create_new_learning_space_description
import org.openeel.shared.viewmodel.manageuser.getstarted.NewGetStartedViewModel


@Composable
fun NewGetStartedScreen(
    viewModel: NewGetStartedViewModel
) {
    NewGetStartedScreen(
        onClickConnectExistingLearningSpace = viewModel::onClickConnectExistingLearningSpace,
    )
}


@Composable
fun NewGetStartedScreen(
    onClickConnectExistingLearningSpace: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .defaultItemPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LearningSpaceCard(
            headline = stringResource(Res.string.connect_existing_learning_space),
            subtitle = stringResource(Res.string.connect_existing_learning_space_description),
            image = AppRes.drawable.connect_space,
            modifier = Modifier.clickable(onClick = onClickConnectExistingLearningSpace),
        )
        LearningSpaceCard(
            headline = stringResource(Res.string.create_new_learning_space),
            subtitle = stringResource(Res.string.create_new_learning_space_description),
            image = AppRes.drawable.create_space,
        )
    }
}

@Composable
private fun LearningSpaceCard(
    headline: String,
    subtitle: String,
    image: DrawableResource,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(image),
                contentDescription = null,
                modifier = Modifier.size(80.dp),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = headline,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}