package pl.marcin.malocha.flickrrecruitmenttask.presentation.screen.photodetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.marcin.malocha.flickrrecruitmenttask.di.providers.DefaultDispatchers
import pl.marcin.malocha.flickrrecruitmenttask.presentation.navigation.PhotoDetailRoute
import pl.marcin.malocha.flickrrecruitmenttask.presentation.theme.FlickrRecruitmentTaskTheme
import pl.marcin.malocha.flickrrecruitmenttask.rules.FakeImageLoaderRule
import pl.marcin.malocha.flickrrecruitmenttask.utils.isPresent

/**
 * Tests the whole [PhotoDetailScreen] with a real [PhotoDetailViewModel]: state flowing to the UI and navigating back.
 * Visual appearance is covered by screenshot tests.
 */
@RunWith(AndroidJUnit4::class)
class PhotoDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val fakeImageLoaderRule = FakeImageLoaderRule()

    private val title get() = composeTestRule.onNodeWithTag(PhotoDetailScreenTestTags.TITLE)
    private val image get() = composeTestRule.onNodeWithTag(PhotoDetailScreenTestTags.IMAGE)
    private val backButton get() = composeTestRule.onNodeWithTag(PhotoDetailScreenTestTags.BACK_BUTTON)
    private val loadingIndicator get() = composeTestRule.onNodeWithTag(PhotoDetailScreenTestTags.LOADING_INDICATOR)

    private var navigatedBack = false

    private fun setContent(title: String) {
        val viewModel = PhotoDetailViewModel(
            route = PhotoDetailRoute(imageUrl = "https://live.staticflickr.com/65535/111_abc_b.jpg", title = title),
            dispatchers = DefaultDispatchers(),
        )
        composeTestRule.setContent {
            FlickrRecruitmentTaskTheme {
                PhotoDetailScreen(viewModel = viewModel, onNavigateBack = { navigatedBack = true })
            }
        }
    }

    @Test
    fun stateTest_ShowsTitleAndImage() {
        setContent(title = "Sleepy cat")

        title.assertTextEquals("Sleepy cat")
        image.assertIsDisplayed()
    }

    @Test
    fun stateTest_EmptyTitle_ShowsFallbackTitle() {
        setContent(title = "")

        title.assertTextEquals("Untitled")
    }

    @Test
    fun stateTest_ImageLoading_ShowsLoadingIndicator() {
        setContent(title = "Sleepy cat")

        composeTestRule.waitUntil { loadingIndicator.isPresent() }
        loadingIndicator.assertIsDisplayed()
    }

    @Test
    fun stateTest_ImageLoaded_HidesLoadingIndicator() {
        setContent(title = "Sleepy cat")
        composeTestRule.waitUntil { loadingIndicator.isPresent() }

        fakeImageLoaderRule.finishLoading()

        composeTestRule.waitUntil { !loadingIndicator.isPresent() }
        loadingIndicator.assertDoesNotExist()
    }

    @Test
    fun actionsTest_BackClicked_NavigatesBack() {
        setContent(title = "Sleepy cat")

        backButton.performClick()
        composeTestRule.waitForIdle()

        assertThat(navigatedBack).isTrue()
    }
}
