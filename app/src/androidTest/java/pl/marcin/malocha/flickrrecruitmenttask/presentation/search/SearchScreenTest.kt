package pl.marcin.malocha.flickrrecruitmenttask.presentation.search

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.marcin.malocha.flickrrecruitmenttask.presentation.theme.FlickrRecruitmentTaskTheme

/**
 * Verifies that user interactions on [SearchScreen] are sent as [SearchEvent]s.
 * Visual appearance is out of scope here (covered later by screenshot tests).
 */
@RunWith(AndroidJUnit4::class)
class SearchScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = mutableListOf<SearchEvent>()

    private fun setContent(state: SearchUiState = SearchUiState()) {
        composeTestRule.setContent {
            FlickrRecruitmentTaskTheme {
                SearchScreen(
                    state = state,
                    onEvent = { events += it },
                )
            }
        }
    }

    @Test
    fun eventsTest_TypingText_SendsQueryChanged() {
        setContent()

        composeTestRule.onNodeWithTag(SearchScreenTestTags.QUERY_FIELD).performTextInput("cat")

        assertThat(events).containsExactly(SearchEvent.QueryChanged("cat"))
    }

    @Test
    fun eventsTest_SearchButtonClicked_SendsSearchClicked() {
        setContent(SearchUiState(query = "cat"))

        composeTestRule.onNodeWithTag(SearchScreenTestTags.SEARCH_BUTTON).performClick()

        assertThat(events).containsExactly(SearchEvent.SearchClicked)
    }

    @Test
    fun eventsTest_SearchButtonClickedWithBlankQuery_SendsNothing() {
        setContent(SearchUiState(query = ""))

        composeTestRule.onNodeWithTag(SearchScreenTestTags.SEARCH_BUTTON).performClick()

        assertThat(events).isEmpty()
    }

    @Test
    fun eventsTest_KeyboardSearchAction_SendsSearchClicked() {
        setContent(SearchUiState(query = "cat"))

        composeTestRule.onNodeWithTag(SearchScreenTestTags.QUERY_FIELD).performImeAction()

        assertThat(events).containsExactly(SearchEvent.SearchClicked)
    }
}
