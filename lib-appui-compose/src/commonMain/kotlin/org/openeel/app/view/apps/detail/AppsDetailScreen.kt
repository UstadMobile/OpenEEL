package world.respect.app.view.apps.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.app.RespectAsyncImage
import world.respect.app.components.AlternativeLangLinks
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.langMapString
import world.respect.app.components.uiTextStringResource
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.opds.model.Publication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.opds.model.findIcons
import world.respect.shared.ext.alternateLanguageLinks
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_app
import world.respect.shared.generated.resources.google_play
import world.respect.shared.generated.resources.lessons
import world.respect.shared.viewmodel.apps.detail.AppsDetailUiState
import world.respect.shared.viewmodel.apps.detail.AppsDetailViewModel

@Composable
fun AppsDetailScreen(
    viewModel: AppsDetailViewModel
) {

    val uiState by viewModel.uiState.collectAsState()

    AppsDetailScreen(
        uiState = uiState,
        onClickLessonList = { viewModel.onClickLessonList() },
        onClickPublication = { viewModel.onClickPublication(it) },
        onClickNavigation = { viewModel.onClickNavigation(it) },
        onClickAdd = { viewModel.onClickAdd() },
        onClickHighlightCard = { viewModel.onClickHighlightCard(it) },
        onClickLicense = { viewModel.onClickLicense(it) },
        onClickGooglePlay = { viewModel.onClickGooglePlay(it) },
        onClickAlternativeLangVersion = viewModel::onClickAlternativeLangVersion,
    )
}

@Composable
fun AppsDetailScreen(
    uiState: AppsDetailUiState,
    onClickLessonList: () -> Unit,
    onClickPublication: (Publication) -> Unit,
    onClickNavigation: (ReadiumLink) -> Unit,
    onClickAdd: () -> Unit,
    onClickHighlightCard: (String) -> Unit,
    onClickLicense: (String) -> Unit,
    onClickGooglePlay: (String) -> Unit,
    onClickAlternativeLangVersion: (ReadiumLink) -> Unit,
) {

    val appDetail = (uiState.appDetail as? DataReadyState)?.data

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        Row(Modifier.defaultItemPadding(top = 16.dp)) {
            appDetail?.findIcons()?.firstOrNull()?.also {
                RespectAsyncImage(
                    uri = it.href,
                    contentDescription = "",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(80.dp)
                )
            }


            Text(
                text = appDetail?.metadata?.title?.let { langMapString(it) } ?: "",
                modifier = Modifier.defaultItemPadding()
            )

        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 4,
            modifier = Modifier.defaultItemPadding().fillMaxWidth()
        ) {

            uiState.licenseLink?.also { licenseLink ->
                uiState.licenseLabelResult?.also { licenseLabelResult ->
                    TextButton(
                        onClick = { onClickLicense(licenseLink.href) },
                    ) {
                        Icon(imageVector = Icons.Filled.Balance, contentDescription = null)

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(text = uiTextStringResource(licenseLabelResult.title))
                    }
                }
            }

            uiState.googlePlayLink?.also { googlePlayLink ->
                TextButton(
                    onClick = { onClickGooglePlay(googlePlayLink.href) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shop,
                        contentDescription = stringResource(Res.string.google_play),
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    Text(googlePlayLink.title ?: "")
                }
            }

        }
        Row(
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            if (!uiState.isAdded && uiState.showAddRemoveButton) {
                OutlinedButton(
                    onClick = onClickAdd,
                    modifier = Modifier.weight(1f).defaultItemPadding(),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Res.string.add_app))
                }
            }
        }

        if (uiState.highlightCards.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { uiState.highlightCards.size })

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().defaultItemPadding()
            ) {

                HorizontalDivider(
                    thickness = 1.dp,
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                )

                HorizontalPager(
                    state = pagerState,
                    pageSize = PageSize.Fixed(240.dp),
                    pageSpacing = 16.dp,
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) { index ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                        modifier = Modifier
                            .size(width = 240.dp, height = 100.dp)
                            .padding(top = 16.dp)
                            .clickable {
                                onClickHighlightCard(uiState.highlightCards[index].href)
                            }
                    ) {
                        Text(
                            text = uiState.highlightCards[index].title.orEmpty(),
                            modifier = Modifier
                                .padding(16.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                )

            }
        }

        appDetail?.links?.alternateLanguageLinks()?.takeIf { it.isNotEmpty() }?.also { altLangLinks ->
            HorizontalDivider()
            AlternativeLangLinks(
                altLangLinks = altLangLinks,
                onClickAlternativeLangVersion = onClickAlternativeLangVersion,
                modifier = Modifier.defaultItemPadding(),
            )
            HorizontalDivider()
        }

        Text(
            text = appDetail?.metadata?.description.orEmpty(),
            modifier = Modifier.defaultItemPadding()
        )

        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(Res.string.lessons),
                    fontWeight = FontWeight.Bold
                )
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClickLessonList() }
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            itemsIndexed(
                items = uiState.navigation,
                key = { _, navigation ->
                    navigation.href
                }
            ) { _, navigation ->
                NavigationList(
                    navigation,
                    onClickNavigation = {
                        onClickNavigation(navigation)
                    }
                )
            }

            itemsIndexed(
                items = uiState.publications,
                key = { _, publication ->
                    publication.metadata.identifier.toString()
                }
            ) { _, publication ->
                PublicationList(
                    publication,
                    onClickPublication = {
                        onClickPublication(publication)
                    }
                )
            }

            uiState.group.forEach { _ ->
                itemsIndexed(
                    items = uiState.navigation,
                    key = { _, navigation ->
                        navigation.href
                    }
                ) { _, navigation ->
                    NavigationList(
                        navigation,
                        onClickNavigation = {
                            onClickNavigation(navigation)
                        }
                    )
                }

                itemsIndexed(
                    items = uiState.publications,
                    key = { _, publication ->
                        publication.metadata.identifier.toString()
                    }
                ) { _, publication ->
                    PublicationList(
                        publication,
                        onClickPublication = {
                            onClickPublication(publication)
                        }
                    )
                }
            }
        }


    }
}

@Composable
fun NavigationList(
    navigation: ReadiumLink,
    onClickNavigation: (ReadiumLink) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clickable {
                onClickNavigation(navigation)
            }
    ) {
        val iconUrl = navigation.alternate?.find {
            it.rel?.contains("icon") == true
        }?.href

        iconUrl.also { icon ->
            RespectAsyncImage(
                uri = icon,
                contentDescription = "",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = navigation.title.toString(),
            maxLines = 3,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun PublicationList(
    publication: Publication, onClickPublication: (Publication) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clickable { onClickPublication(publication) }
    ) {
        val iconUrl = publication.images?.firstOrNull()?.href

        iconUrl.also { icon ->
            RespectAsyncImage(
                uri = icon,
                contentDescription = "",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(8.dp))

            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = langMapString(publication.metadata.title),
            maxLines = 1,
        )
    }
}
