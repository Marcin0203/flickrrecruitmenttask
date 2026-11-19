package pl.marcin.malocha.flickrrecruitmenttask.presentation.screen.search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import kotlinx.coroutines.CompletableDeferred
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.marcin.malocha.flickrrecruitmenttask.di.providers.DefaultDispatchers
import pl.marcin.malocha.flickrrecruitmenttask.domain.model.Photo
import pl.marcin.malocha.flickrrecruitmenttask.domain.usecase.SearchPhotosUseCase
import pl.marcin.malocha.flickrrecruitmenttask.presentation.mapper.toUi
import pl.marcin.malocha.flickrrecruitmenttask.presentation.model.PhotoUi
import pl.marcin.malocha.flickrrecruitmenttask.presentation.theme.FlickrRecruitmentTaskTheme
import pl.marcin.malocha.flickrrecruitmenttask.rules.FakeImageLoaderRule
import pl.marcin.malocha.flickrrecruitmenttask.utils.isPresent
import java.io.IOException

/**
 * Tests the whole [SearchScreen] with a real [SearchViewModel]: state flowing to the UI and handling of one-off actions.
 * Visual appearance is covered by screenshot tests.
 */
@RunWith(AndroidJUnit4::class)
class SearchScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val fakeImageLoaderRule = FakeImageLoaderRule()

    private val queryField get() = composeTestRule.onNodeWithTag(SearchScreenTestTags.QUERY_FIELD)
    private val searchButton get() = composeTestRule.onNodeWithTag(SearchScreenTestTags.SEARCH_BUTTON)
    private val loadingIndicator get() = composeTestRule.onNodeWithTag(SearchScreenTestTags.LOADING_INDICATOR)
    private val errorMessage get() = composeTestRule.onNodeWithTag(SearchScreenTestTags.ERROR_MESSAGE)
    private val noResultsMessage get() = composeTestRule.onNodeWithTag(SearchScreenTestTags.NO_RESULTS_MESSAGE)
    private val resultsGrid get() = composeTestRule.onNodeWithTag(SearchScreenTestTags.RESULTS_GRID)
    private val photoItems get() = composeTestRule.onAllNodesWithTag(SearchScreenTestTags.PHOTO_ITEM)
    private val photoLoadingIndicator get() = composeTestRule.onNodeWithTag(SearchScreenTestTags.PHOTO_LOADING_INDICATOR)

    private fun photo(id: String, title: String) = Photo(
        id = "https://www.flickr.com/photos/user/$id/",
        title = title,
        thumbnailUrl = "https://live.staticflickr.com/65535/${id}_abc_m.jpg",
        fullSizeUrl = "https://live.staticflickr.com/65535/${id}_abc_b.jpg",
    )

    private val searchPhotos: SearchPhotosUseCase = mockk()
    private val navigatedToPhotos = mutableListOf<PhotoUi>()

    @Before
    fun setUp() {
        coEvery { searchPhotos(any()) } returns Result.success(emptyList())
        val viewModel = SearchViewModel(
            searchPhotos = searchPhotos,
            dispatchers = DefaultDispatchers(),
        )
        composeTestRule.setContent {
            FlickrRecruitmentTaskTheme {
                SearchScreen(onNavigateToPhoto = { navigatedToPhotos += it }, viewModel = viewModel)
            }
        }
    }

    @Test
    fun stateTest_InitialState_SearchButtonDisabled() {
        searchButton.assertIsNotEnabled()
    }

    @Test
    fun stateTest_InitialState_NoLoadingErrorOrNoResults() {
        loadingIndicator.assertDoesNotExist()
        errorMessage.assertDoesNotExist()
        noResultsMessage.assertDoesNotExist()
    }

    @Test
    fun stateTest_SearchInProgress_ShowsLoadingIndicator() {
        coEvery { searchPhotos(any()) } coAnswers { CompletableDeferred<Result<List<Photo>>>().await() }

        queryField.performTextInput("cat")
        searchButton.performClick()

        loadingIndicator.assertIsDisplayed()
        errorMessage.assertDoesNotExist()
    }

    @Test
    fun stateTest_SearchFailed_ShowsErrorMessage() {
        coEvery { searchPhotos(any()) } returns Result.failure(IOException("No network"))

        queryField.performTextInput("cat")
        searchButton.performClick()

        errorMessage.assertIsDisplayed()
        loadingIndicator.assertDoesNotExist()
    }

    @Test
    fun stateTest_SearchWithoutResults_ShowsNoResultsMessage() {
        queryField.performTextInput("cat")
        searchButton.performClick()

        noResultsMessage.assertIsDisplayed()
        errorMessage.assertDoesNotExist()
    }

    @Test
    fun stateTest_SearchWithResults_ShowsPhotosWithTitles() {
        coEvery { searchPhotos(any()) } returns Result.success(
            listOf(photo(id = "1", title = "Sleepy cat"), photo(id = "2", title = "Happy dog")),
        )

        queryField.performTextInput("cat")
        searchButton.performClick()

        resultsGrid.assertIsDisplayed()
        photoItems.assertCountEquals(2)
        // On a narrow screen the grid has one column, so the second photo can be below the bottom edge.
        resultsGrid.performScrollToNode(hasText("Sleepy cat"))
        composeTestRule.onNodeWithText("Sleepy cat").assertIsDisplayed()
        resultsGrid.performScrollToNode(hasText("Happy dog"))
        composeTestRule.onNodeWithText("Happy dog").assertIsDisplayed()
        noResultsMessage.assertDoesNotExist()
    }

    @Test
    fun stateTest_PhotoLoading_ShowsLoadingIndicator() {
        coEvery { searchPhotos(any()) } returns Result.success(listOf(photo(id = "1", title = "Sleepy cat")))

        queryField.performTextInput("cat")
        searchButton.performClick()

        composeTestRule.waitUntil { photoLoadingIndicator.isPresent() }
        photoLoadingIndicator.assertIsDisplayed()
    }

    @Test
    fun stateTest_PhotoLoaded_HidesLoadingIndicator() {
        coEvery { searchPhotos(any()) } returns Result.success(listOf(photo(id = "1", title = "Sleepy cat")))
        queryField.performTextInput("cat")
        searchButton.performClick()
        composeTestRule.waitUntil { photoLoadingIndicator.isPresent() }

        fakeImageLoaderRule.finishLoading()

        composeTestRule.waitUntil { !photoLoadingIndicator.isPresent() }
        photoLoadingIndicator.assertDoesNotExist()
    }

    @Test
    fun actionsTest_PhotoClicked_NavigatesToPhoto() {
        val photo = photo(id = "1", title = "Sleepy cat")
        coEvery { searchPhotos(any()) } returns Result.success(listOf(photo))
        queryField.performTextInput("cat")
        searchButton.performClick()

        composeTestRule.onNodeWithText("Sleepy cat").performClick()
        composeTestRule.waitForIdle()

        assertThat(navigatedToPhotos).containsExactly(photo.toUi())
    }

    @Test
    fun stateTest_PhotoWithoutTitle_ShowsFallbackTitle() {
        coEvery { searchPhotos(any()) } returns Result.success(listOf(photo(id = "1", title = "")))

        queryField.performTextInput("cat")
        searchButton.performClick()

        composeTestRule.onNodeWithText("Untitled").assertIsDisplayed()
    }

    @Test
    fun stateTest_TypingText_UpdatesFieldAndEnablesSearchButton() {
        queryField.performTextInput("cat")

        queryField.assertTextContains("cat")
        searchButton.assertIsEnabled()
    }

    @Test
    fun actionsTest_KeyboardSearchAction_ClearsFocus() {
        queryField.performTextInput("cat")
        queryField.assertIsFocused()

        queryField.performImeAction()

        queryField.assertIsNotFocused()
    }

    @Test
    fun actionsTest_SearchButtonClicked_ClearsFocus() {
        queryField.performTextInput("cat")
        queryField.assertIsFocused()

        searchButton.performClick()

        queryField.assertIsNotFocused()
    }

    @Test
    fun actionsTest_KeyboardSearchActionWithBlankQuery_KeepsFocus() {
        queryField.performClick()
        queryField.assertIsFocused()

        queryField.performImeAction()

        queryField.assertIsFocused()
    }
}
