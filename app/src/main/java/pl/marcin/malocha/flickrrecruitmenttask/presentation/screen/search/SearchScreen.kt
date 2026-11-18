package pl.marcin.malocha.flickrrecruitmenttask.presentation.screen.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import pl.marcin.malocha.flickrrecruitmenttask.R
import pl.marcin.malocha.flickrrecruitmenttask.domain.model.Photo
import pl.marcin.malocha.flickrrecruitmenttask.presentation.theme.FlickrRecruitmentTaskTheme
import pl.marcin.malocha.flickrrecruitmenttask.presentation.utils.ExcludeFromJacocoGeneratedReport

object SearchScreenTestTags {
    const val QUERY_FIELD = "search_query_field"
    const val SEARCH_BUTTON = "search_button"
    const val LOADING_INDICATOR = "search_loading_indicator"
    const val ERROR_MESSAGE = "search_error_message"
    const val NO_RESULTS_MESSAGE = "search_no_results_message"
    const val RESULTS_GRID = "search_results_grid"
    const val PHOTO_ITEM = "search_photo_item"
}

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(viewModel) {
        viewModel.actions.collect { action ->
            when (action) {
                SearchAction.HideKeyboard -> {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            }
        }
    }

    SearchScreenLayout(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

@Composable
internal fun SearchScreenLayout(
    state: SearchUiState,
    onEvent: (SearchEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { onEvent(SearchEvent.QueryChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(SearchScreenTestTags.QUERY_FIELD),
                label = { Text(stringResource(R.string.search_tags_label)) },
                placeholder = { Text(stringResource(R.string.search_tags_placeholder)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onEvent(SearchEvent.SearchClicked) }),
            )
            Button(
                onClick = { onEvent(SearchEvent.SearchClicked) },
                enabled = state.isSearchEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(SearchScreenTestTags.SEARCH_BUTTON),
            ) {
                Text(stringResource(R.string.search_button))
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    state.isLoading -> CircularProgressIndicator(
                        modifier = Modifier.testTag(SearchScreenTestTags.LOADING_INDICATOR),
                    )
                    state.isError -> SearchMessage(
                        text = stringResource(R.string.search_error),
                        modifier = Modifier.testTag(SearchScreenTestTags.ERROR_MESSAGE),
                    )
                    state.hasNoResults -> SearchMessage(
                        text = stringResource(R.string.search_no_results),
                        modifier = Modifier.testTag(SearchScreenTestTags.NO_RESULTS_MESSAGE),
                    )
                    else -> PhotoGrid(photos = state.photos)
                }
            }
        }
    }
}

@Composable
private fun PhotoGrid(
    photos: List<Photo>,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag(SearchScreenTestTags.RESULTS_GRID),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(photos, key = { it.id }) { photo ->
            PhotoGridItem(photo = photo)
        }
    }
}

@Composable
private fun PhotoGridItem(
    photo: Photo,
    modifier: Modifier = Modifier,
) {
    val title = if (photo.hasTitle) photo.title else stringResource(R.string.photo_untitled)
    Card(modifier = modifier.testTag(SearchScreenTestTags.PHOTO_ITEM)) {
        AsyncImage(
            model = photo.thumbnailUrl,
            contentDescription = title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                // Visible until the image is loaded.
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentScale = ContentScale.Crop,
        )
        Text(
            text = title,
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.bodyMedium,
            // Two lines for every title, so all cards in a row have the same height.
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SearchMessage(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
    )
}

@ExcludeFromJacocoGeneratedReport
@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() {
    FlickrRecruitmentTaskTheme {
        SearchScreenLayout(
            state = SearchUiState(query = "cat, dog"),
            onEvent = {},
        )
    }
}

@ExcludeFromJacocoGeneratedReport
@Preview(showBackground = true)
@Composable
private fun SearchScreenLoadingPreview() {
    FlickrRecruitmentTaskTheme {
        SearchScreenLayout(
            state = SearchUiState(query = "cat, dog", isLoading = true),
            onEvent = {},
        )
    }
}

@ExcludeFromJacocoGeneratedReport
@Preview(showBackground = true)
@Composable
private fun SearchScreenErrorPreview() {
    FlickrRecruitmentTaskTheme {
        SearchScreenLayout(
            state = SearchUiState(query = "cat, dog", isError = true, hasSearched = true),
            onEvent = {},
        )
    }
}

@ExcludeFromJacocoGeneratedReport
@Preview(showBackground = true)
@Composable
private fun SearchScreenNoResultsPreview() {
    FlickrRecruitmentTaskTheme {
        SearchScreenLayout(
            state = SearchUiState(query = "cat, dog", hasSearched = true),
            onEvent = {},
        )
    }
}

@ExcludeFromJacocoGeneratedReport
@Preview(showBackground = true)
@Composable
private fun SearchScreenResultsPreview() {
    FlickrRecruitmentTaskTheme {
        SearchScreenLayout(
            state = SearchUiState(
                query = "cat, dog",
                photos = List(4) { index ->
                    Photo(
                        id = "photo_$index",
                        title = if (index == 1) "" else "Photo $index",
                        thumbnailUrl = "",
                        fullSizeUrl = "",
                    )
                },
                hasSearched = true,
            ),
            onEvent = {},
        )
    }
}
