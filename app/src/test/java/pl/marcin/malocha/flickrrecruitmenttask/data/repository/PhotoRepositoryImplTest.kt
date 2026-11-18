package pl.marcin.malocha.flickrrecruitmenttask.data.repository

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import pl.marcin.malocha.flickrrecruitmenttask.data.remote.datasource.PhotoRemoteDataSource
import pl.marcin.malocha.flickrrecruitmenttask.data.remote.dto.FeedPhotoDto
import pl.marcin.malocha.flickrrecruitmenttask.data.remote.dto.FeedPhotoMediaDto
import pl.marcin.malocha.flickrrecruitmenttask.data.remote.dto.PublicPhotosFeedDto
import pl.marcin.malocha.flickrrecruitmenttask.domain.model.Photo
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class PhotoRepositoryImplTest {

    private val remoteDataSource: PhotoRemoteDataSource = mockk(relaxed = true)

    private fun createRepository(): PhotoRepositoryImpl =
        PhotoRepositoryImpl(remoteDataSource = remoteDataSource)

    @Test
    fun searchPhotosTest_PassesTagsToRemoteDataSource() = runTest {
        createRepository().searchPhotos(listOf("cat", "dog"))

        coVerify(exactly = 1) { remoteDataSource.getPublicPhotos(listOf("cat", "dog")) }
    }

    @Test
    fun searchPhotosTest_Success_ReturnsMappedPhotos() = runTest {
        coEvery { remoteDataSource.getPublicPhotos(any()) } returns PublicPhotosFeedDto(
            items = listOf(
                FeedPhotoDto(
                    title = "Sleepy cat",
                    link = "https://www.flickr.com/photos/user/111/",
                    media = FeedPhotoMediaDto(small = "https://live.staticflickr.com/65535/111_abc_m.jpg"),
                ),
            ),
        )

        val result = createRepository().searchPhotos(listOf("cat"))

        assertThat(result.getOrNull()).containsExactly(
            Photo(
                id = "https://www.flickr.com/photos/user/111/",
                title = "Sleepy cat",
                thumbnailUrl = "https://live.staticflickr.com/65535/111_abc_m.jpg",
                fullSizeUrl = "https://live.staticflickr.com/65535/111_abc_b.jpg",
            ),
        )
    }

    @Test
    fun searchPhotosTest_EmptyFeed_ReturnsEmptyList() = runTest {
        coEvery { remoteDataSource.getPublicPhotos(any()) } returns PublicPhotosFeedDto(items = emptyList())

        val result = createRepository().searchPhotos(listOf("cat"))

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEmpty()
    }

    @Test
    fun searchPhotosTest_RemoteThrows_ReturnsFailure() = runTest {
        coEvery { remoteDataSource.getPublicPhotos(any()) } throws IOException("No network")

        val result = createRepository().searchPhotos(listOf("cat"))

        assertThat(result.isFailure).isTrue()
        // Coroutines may copy the exception to recover its stack trace, so type and message are checked, not the instance.
        assertThat(result.exceptionOrNull()).isInstanceOf(IOException::class.java)
        assertThat(result.exceptionOrNull()).hasMessageThat().isEqualTo("No network")
    }

    @Test
    fun searchPhotosTest_Cancellation_IsRethrown() = runTest {
        coEvery { remoteDataSource.getPublicPhotos(any()) } throws CancellationException("Cancelled")

        val thrown = try {
            createRepository().searchPhotos(listOf("cat"))
            null
        } catch (e: CancellationException) {
            e
        }

        assertThat(thrown).isInstanceOf(CancellationException::class.java)
    }
}
