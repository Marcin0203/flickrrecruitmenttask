package pl.marcin.malocha.flickrrecruitmenttask.presentation.screen.photodetail

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import pl.marcin.malocha.flickrrecruitmenttask.di.providers.UnconfinedTestDispatcher
import pl.marcin.malocha.flickrrecruitmenttask.presentation.navigation.PhotoDetailRoute
import pl.marcin.malocha.flickrrecruitmenttask.rules.MainDispatcherRule
import pl.marcin.malocha.flickrrecruitmenttask.utils.collectToList

class PhotoDetailViewModelTest {

    private val scheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(scheduler = scheduler)

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private fun createViewModel(
        title: String = "Sleepy cat",
    ): PhotoDetailViewModel = PhotoDetailViewModel(
        route = PhotoDetailRoute(
            imageUrl = "https://live.staticflickr.com/65535/111_abc_b.jpg",
            title = title,
        ),
        dispatchers = UnconfinedTestDispatcher(scheduler),
    )

    @Test
    fun initialStateTest_TakesPhotoFromRoute() {
        val state = createViewModel().state.value

        assertThat(state.imageUrl).isEqualTo("https://live.staticflickr.com/65535/111_abc_b.jpg")
        assertThat(state.title).isEqualTo("Sleepy cat")
        assertThat(state.hasTitle).isTrue()
    }

    @Test
    fun initialStateTest_EmptyTitle_HasNoTitle() {
        assertThat(createViewModel(title = "").state.value.hasTitle).isFalse()
    }

    @Test
    fun initialStateTest_BlankTitle_HasNoTitle() {
        assertThat(createViewModel(title = "   ").state.value.hasTitle).isFalse()
    }

    @Test
    fun eventsTest_BackClicked_NavigatesBack() = runTest(scheduler) {
        val viewModel = createViewModel()
        val actions = collectToList(viewModel.actions)

        viewModel.onEvent(PhotoDetailEvent.BackClicked)

        assertThat(actions).containsExactly(PhotoDetailAction.NavigateBack)
    }
}
