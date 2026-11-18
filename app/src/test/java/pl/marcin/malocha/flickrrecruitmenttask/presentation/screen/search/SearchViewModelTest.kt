package pl.marcin.malocha.flickrrecruitmenttask.presentation.screen.search

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.marcin.malocha.flickrrecruitmenttask.di.providers.TestDispatchers
import pl.marcin.malocha.flickrrecruitmenttask.di.providers.UnconfinedTestDispatcher
import pl.marcin.malocha.flickrrecruitmenttask.domain.model.Photo
import pl.marcin.malocha.flickrrecruitmenttask.domain.usecase.SearchPhotosUseCase
import pl.marcin.malocha.flickrrecruitmenttask.rules.MainDispatcherRule
import pl.marcin.malocha.flickrrecruitmenttask.utils.collectToList
import java.io.IOException

class SearchViewModelTest {

    lateinit var viewModel: SearchViewModel
    private val scheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(scheduler = scheduler)
    private val searchPhotos: SearchPhotosUseCase = mockk()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    @Before
    fun setUp() {
        coEvery { searchPhotos(any()) } returns Result.success(emptyList())
        viewModel = createViewModel()
    }

    private fun createViewModel(
        testDispatchers: TestDispatchers = UnconfinedTestDispatcher(scheduler)
    ): SearchViewModel {
        return SearchViewModel(searchPhotos = searchPhotos, dispatchers = testDispatchers)
    }

    private fun photo(id: String) = Photo(
        id = "https://www.flickr.com/photos/user/$id/",
        title = "Photo $id",
        thumbnailUrl = "https://live.staticflickr.com/65535/${id}_abc_m.jpg",
        fullSizeUrl = "https://live.staticflickr.com/65535/${id}_abc_b.jpg",
    )

    private fun search(query: String) {
        viewModel.onEvent(SearchEvent.QueryChanged(query))
        viewModel.onEvent(SearchEvent.SearchClicked)
    }

    @Test
    fun initialStateTest() {
        val state = viewModel.state.value

        assertThat(state.query).isEmpty()
        assertThat(state.isSearchEnabled).isFalse()
        assertThat(state.photos).isEmpty()
        assertThat(state.isLoading).isFalse()
        assertThat(state.isError).isFalse()
        assertThat(state.hasSearched).isFalse()
        assertThat(state.hasNoResults).isFalse()
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

        search("cat, dog")

        assertThat(actions).containsExactly(SearchAction.HideKeyboard)
        coVerify(exactly = 1) { searchPhotos("cat, dog") }
    }

    @Test
    fun eventsTest_SearchClicked_emptyQuery() = runTest(scheduler) {
        val actions = collectToList(viewModel.actions)

        viewModel.onEvent(SearchEvent.SearchClicked)
        scheduler.advanceUntilIdle()

        assertThat(actions).isEmpty()
        coVerify(exactly = 0) { searchPhotos(any()) }
    }

    @Test
    fun eventsTest_SearchClicked_blankQuery() = runTest(scheduler) {
        val actions = collectToList(viewModel.actions)

        search("   ")
        scheduler.advanceUntilIdle()

        assertThat(actions).isEmpty()
        coVerify(exactly = 0) { searchPhotos(any()) }
    }

    @Test
    fun searchTest_InProgress_IsLoading() = runTest(scheduler) {
        val result = CompletableDeferred<Result<List<Photo>>>()
        coEvery { searchPhotos(any()) } coAnswers { result.await() }

        search("cat")

        assertThat(viewModel.state.value.isLoading).isTrue()
        assertThat(viewModel.state.value.hasSearched).isFalse()
        assertThat(viewModel.state.value.hasNoResults).isFalse()

        result.complete(Result.success(emptyList()))

        assertThat(viewModel.state.value.isLoading).isFalse()
    }

    @Test
    fun searchTest_Success_ShowsPhotos() = runTest(scheduler) {
        val photos = listOf(photo("1"), photo("2"))
        coEvery { searchPhotos(any()) } returns Result.success(photos)

        search("cat")

        val state = viewModel.state.value
        assertThat(state.photos).isEqualTo(photos)
        assertThat(state.isLoading).isFalse()
        assertThat(state.isError).isFalse()
        assertThat(state.hasSearched).isTrue()
        assertThat(state.hasNoResults).isFalse()
    }

    @Test
    fun searchTest_NoResults_HasSearchedWithEmptyPhotos() = runTest(scheduler) {
        search("cat")

        val state = viewModel.state.value
        assertThat(state.photos).isEmpty()
        assertThat(state.isError).isFalse()
        assertThat(state.hasSearched).isTrue()
        assertThat(state.hasNoResults).isTrue()
    }

    @Test
    fun searchTest_Failure_ShowsErrorAndClearsPhotos() = runTest(scheduler) {
        coEvery { searchPhotos(any()) } returns Result.success(listOf(photo("1")))
        search("cat")
        coEvery { searchPhotos(any()) } returns Result.failure(IOException("No network"))

        search("dog")

        val state = viewModel.state.value
        assertThat(state.photos).isEmpty()
        assertThat(state.isLoading).isFalse()
        assertThat(state.isError).isTrue()
        assertThat(state.hasSearched).isTrue()
        assertThat(state.hasNoResults).isFalse()
    }

    @Test
    fun searchTest_SuccessAfterFailure_ClearsError() = runTest(scheduler) {
        coEvery { searchPhotos(any()) } returns Result.failure(IOException("No network"))
        search("cat")
        coEvery { searchPhotos(any()) } returns Result.success(listOf(photo("1")))

        search("cat")

        assertThat(viewModel.state.value.isError).isFalse()
        assertThat(viewModel.state.value.photos).containsExactly(photo("1"))
    }

    @Test
    fun searchTest_NewSearch_CancelsPreviousOne() = runTest(scheduler) {
        val firstResult = CompletableDeferred<Result<List<Photo>>>()
        coEvery { searchPhotos("cat") } coAnswers { firstResult.await() }
        coEvery { searchPhotos("dog") } returns Result.success(listOf(photo("dog")))

        search("cat")
        search("dog")
        firstResult.complete(Result.success(listOf(photo("cat"))))
        scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.photos).containsExactly(photo("dog"))
        assertThat(viewModel.state.value.isLoading).isFalse()
    }
}
