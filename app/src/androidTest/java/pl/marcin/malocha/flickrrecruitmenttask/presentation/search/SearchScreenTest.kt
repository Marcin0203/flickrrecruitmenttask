package pl.marcin.malocha.flickrrecruitmenttask.presentation.search

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.marcin.malocha.flickrrecruitmenttask.di.providers.DefaultDispatchers
import pl.marcin.malocha.flickrrecruitmenttask.domain.usecase.SearchPhotosUseCase
import pl.marcin.malocha.flickrrecruitmenttask.presentation.theme.FlickrRecruitmentTaskTheme

/**
 * Tests the whole [SearchScreen] with a real [SearchViewModel]: state flowing to the UI and handling of one-off actions.
 * Visual appearance is covered by screenshot tests.
 */
@RunWith(AndroidJUnit4::class)
class SearchScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val queryField get() = composeTestRule.onNodeWithTag(SearchScreenTestTags.QUERY_FIELD)
    private val searchButton get() = composeTestRule.onNodeWithTag(SearchScreenTestTags.SEARCH_BUTTON)

    private val searchPhotos: SearchPhotosUseCase = mockk()

    @Before
    fun setUp() {
        coEvery { searchPhotos(any()) } returns Result.success(emptyList())
        val viewModel = SearchViewModel(
            searchPhotos = searchPhotos,
            dispatchers = DefaultDispatchers(),
        )
        composeTestRule.setContent {
            FlickrRecruitmentTaskTheme {
                SearchScreen(viewModel = viewModel)
            }
        }
    }

    @Test
    fun stateTest_InitialState_SearchButtonDisabled() {
        searchButton.assertIsNotEnabled()
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
