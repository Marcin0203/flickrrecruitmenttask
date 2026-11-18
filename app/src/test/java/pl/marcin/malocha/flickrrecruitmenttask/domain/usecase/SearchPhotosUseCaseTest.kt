package pl.marcin.malocha.flickrrecruitmenttask.domain.usecase

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import pl.marcin.malocha.flickrrecruitmenttask.domain.model.Photo
import pl.marcin.malocha.flickrrecruitmenttask.domain.repository.PhotoRepository
import java.io.IOException

class SearchPhotosUseCaseTest {

    private val repository: PhotoRepository = mockk(relaxed = true)

    private fun createUseCase(): SearchPhotosUseCase =
        SearchPhotosUseCase(repository = repository)

    @Test
    fun invokeTest_CommaSeparatedQuery_PassesTagsToRepository() = runTest {
        createUseCase()("cat, dog")

        coVerify(exactly = 1) { repository.searchPhotos(listOf("cat", "dog")) }
    }

    @Test
    fun invokeTest_SpaceSeparatedQuery_PassesTagsToRepository() = runTest {
        createUseCase()("  cat   dog ")

        coVerify(exactly = 1) { repository.searchPhotos(listOf("cat", "dog")) }
    }

    @Test
    fun invokeTest_EmptyTagsBetweenSeparators_AreSkipped() = runTest {
        createUseCase()("cat,, ,dog")

        coVerify(exactly = 1) { repository.searchPhotos(listOf("cat", "dog")) }
    }

    @Test
    fun invokeTest_DuplicateTags_ArePassedOnce() = runTest {
        createUseCase()("cat, dog, cat")

        coVerify(exactly = 1) { repository.searchPhotos(listOf("cat", "dog")) }
    }

    @Test
    fun invokeTest_QueryWithoutTags_ReturnsEmptyListWithoutCallingRepository() = runTest {
        val result = createUseCase()(" , ,  ")

        assertThat(result.getOrNull()).isEmpty()
        coVerify(exactly = 0) { repository.searchPhotos(any()) }
    }

    @Test
    fun invokeTest_RepositorySuccess_ReturnsPhotos() = runTest {
        val photos = listOf(
            Photo(
                id = "https://www.flickr.com/photos/user/111/",
                title = "Sleepy cat",
                thumbnailUrl = "https://live.staticflickr.com/65535/111_abc_m.jpg",
                fullSizeUrl = "https://live.staticflickr.com/65535/111_abc_b.jpg",
            ),
        )
        coEvery { repository.searchPhotos(any()) } returns Result.success(photos)

        val result = createUseCase()("cat")

        assertThat(result.getOrNull()).isEqualTo(photos)
    }

    @Test
    fun invokeTest_RepositoryFailure_ReturnsFailure() = runTest {
        val error = IOException("No network")
        coEvery { repository.searchPhotos(any()) } returns Result.failure(error)

        val result = createUseCase()("cat")

        assertThat(result.exceptionOrNull()).isSameInstanceAs(error)
    }
}
