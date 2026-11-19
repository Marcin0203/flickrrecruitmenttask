package pl.marcin.malocha.flickrrecruitmenttask.presentation.screen.photodetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import pl.marcin.malocha.flickrrecruitmenttask.R
import pl.marcin.malocha.flickrrecruitmenttask.presentation.theme.FlickrRecruitmentTaskTheme
import pl.marcin.malocha.flickrrecruitmenttask.presentation.utils.ExcludeFromJacocoGeneratedReport

object PhotoDetailScreenTestTags {
    const val BACK_BUTTON = "photo_detail_back_button"
    const val TITLE = "photo_detail_title"
    const val IMAGE = "photo_detail_image"
    const val LOADING_INDICATOR = "photo_detail_loading_indicator"
}

@Composable
fun PhotoDetailScreen(
    viewModel: PhotoDetailViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.actions.collect { action ->
            when (action) {
                PhotoDetailAction.NavigateBack -> onNavigateBack()
            }
        }
    }

    PhotoDetailScreenLayout(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PhotoDetailScreenLayout(
    state: PhotoDetailUiState,
    onEvent: (PhotoDetailEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = if (state.hasTitle) state.title else stringResource(R.string.photo_untitled)
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        modifier = Modifier.testTag(PhotoDetailScreenTestTags.TITLE),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onEvent(PhotoDetailEvent.BackClicked) },
                        modifier = Modifier.testTag(PhotoDetailScreenTestTags.BACK_BUTTON),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.photo_detail_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        SubcomposeAsyncImage(
            model = state.imageUrl,
            contentDescription = title,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .testTag(PhotoDetailScreenTestTags.IMAGE),
            // The whole photo is shown, without cropping.
            contentScale = ContentScale.Fit,
            loading = {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.testTag(PhotoDetailScreenTestTags.LOADING_INDICATOR),
                    )
                }
            },
        )
    }
}

@ExcludeFromJacocoGeneratedReport
@Preview(showBackground = true)
@Composable
private fun PhotoDetailScreenPreview() {
    FlickrRecruitmentTaskTheme {
        PhotoDetailScreenLayout(
            state = PhotoDetailUiState(imageUrl = "", title = "Sleepy cat"),
            onEvent = {},
        )
    }
}

@ExcludeFromJacocoGeneratedReport
@Preview(showBackground = true)
@Composable
private fun PhotoDetailScreenWithoutTitlePreview() {
    FlickrRecruitmentTaskTheme {
        PhotoDetailScreenLayout(
            state = PhotoDetailUiState(imageUrl = "", title = ""),
            onEvent = {},
        )
    }
}
