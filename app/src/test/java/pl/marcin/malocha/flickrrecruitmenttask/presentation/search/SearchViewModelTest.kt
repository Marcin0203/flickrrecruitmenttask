package pl.marcin.malocha.flickrrecruitmenttask.presentation.search

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.marcin.malocha.flickrrecruitmenttask.di.providers.TestDispatchers
import pl.marcin.malocha.flickrrecruitmenttask.di.providers.UnconfinedTestDispatcher
import pl.marcin.malocha.flickrrecruitmenttask.rules.MainDispatcherRule
import pl.marcin.malocha.flickrrecruitmenttask.utils.collectToList

class SearchViewModelTest {

    lateinit var viewModel: SearchViewModel
    private val scheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(scheduler = scheduler)

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    @Before
    fun setUp() {
        viewModel = createViewModel()
    }

    private fun createViewModel(
        testDispatchers: TestDispatchers = UnconfinedTestDispatcher(scheduler)
    ): SearchViewModel {
        return SearchViewModel(dispatchers = testDispatchers)
    }

    @Test
    fun initialStateTest() {
        val state = viewModel.state.value

        assertThat(state.query).isEmpty()
        assertThat(state.isSearchEnabled).isFalse()
    }

    @Test
    fun eventsTest_QueryChanged() {
        viewModel.onEvent(SearchEvent.QueryChanged("cat, dog"))

        assertThat(viewModel.state.value.query).isEqualTo("cat, dog")
        assertThat(viewModel.state.value.isSearchEnabled).isTrue()
    }

    @Test
    fun eventsTest_QueryChanged_blank() {
        viewModel.onEvent(SearchEvent.QueryChanged("   "))

        assertThat(viewModel.state.value.query).isEqualTo("   ")
        assertThat(viewModel.state.value.isSearchEnabled).isFalse()
    }

    @Test
    fun eventsTest_SearchClicked_validQuery() = runTest(scheduler) {
        val actions = collectToList(viewModel.actions)

        viewModel.onEvent(SearchEvent.QueryChanged("cat"))
        viewModel.onEvent(SearchEvent.SearchClicked)

        assertThat(actions).containsExactly(SearchAction.HideKeyboard)
    }

    @Test
    fun eventsTest_SearchClicked_emptyQuery() = runTest(scheduler) {
        val actions = collectToList(viewModel.actions)

        viewModel.onEvent(SearchEvent.SearchClicked)
        scheduler.advanceUntilIdle()

        assertThat(actions).isEmpty()
    }

    @Test
    fun eventsTest_SearchClicked_blankQuery() = runTest(scheduler) {
        val actions = collectToList(viewModel.actions)

        viewModel.onEvent(SearchEvent.QueryChanged("   "))
        viewModel.onEvent(SearchEvent.SearchClicked)
        scheduler.advanceUntilIdle()

        assertThat(actions).isEmpty()
    }
}
