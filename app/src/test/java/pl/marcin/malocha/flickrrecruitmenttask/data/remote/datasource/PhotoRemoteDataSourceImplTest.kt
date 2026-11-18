package pl.marcin.malocha.flickrrecruitmenttask.data.remote.datasource

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Test
import pl.marcin.malocha.flickrrecruitmenttask.data.remote.FlickrApi
import pl.marcin.malocha.flickrrecruitmenttask.data.remote.dto.FeedPhotoDto
import pl.marcin.malocha.flickrrecruitmenttask.data.remote.dto.FeedPhotoMediaDto
import pl.marcin.malocha.flickrrecruitmenttask.data.remote.dto.PublicPhotosFeedDto
import pl.marcin.malocha.flickrrecruitmenttask.di.providers.StandardTestDispatcher
import pl.marcin.malocha.flickrrecruitmenttask.di.providers.TestDispatchers
import java.io.IOException

class PhotoRemoteDataSourceImplTest {

    private val scheduler = TestCoroutineScheduler()
    private val testDispatchers: TestDispatchers = StandardTestDispatcher(scheduler)
    private val api: FlickrApi = mockk(relaxed = true)

    private fun createDataSource(): PhotoRemoteDataSourceImpl =
        PhotoRemoteDataSourceImpl(api = api, dispatcherProvider = testDispatchers)

    @Test
    fun getPublicPhotosTest_JoinsTagsWithComma() = runTest(testDispatchers.testDispatchers) {
        createDataSource().getPublicPhotos(listOf("cat", "dog"))

        coVerify(exactly = 1) { api.getPublicPhotos("cat,dog") }
    }

    @Test
    fun getPublicPhotosTest_SingleTag_PassedAsIs() = runTest(testDispatchers.testDispatchers) {
        createDataSource().getPublicPhotos(listOf("cat"))

        coVerify(exactly = 1) { api.getPublicPhotos("cat") }
    }

    @Test
    fun getPublicPhotosTest_ReturnsApiResponse() = runTest(testDispatchers.testDispatchers) {
        val feed = PublicPhotosFeedDto(
            items = listOf(
                FeedPhotoDto(
                    title = "Sleepy cat",
                    link = "https://www.flickr.com/photos/user/111/",
                    media = FeedPhotoMediaDto(small = "https://live.staticflickr.com/65535/111_abc_m.jpg"),
                ),
            ),
        )
        coEvery { api.getPublicPhotos(any()) } returns feed

        val result = createDataSource().getPublicPhotos(listOf("cat"))

        assertThat(result).isEqualTo(feed)
    }

    @Test
    fun getPublicPhotosTest_ApiThrows_ExceptionPropagates() = runTest(testDispatchers.testDispatchers) {
        coEvery { api.getPublicPhotos(any()) } throws IOException("No network")

        val thrown = runCatching { createDataSource().getPublicPhotos(listOf("cat")) }.exceptionOrNull()

        // Coroutines may copy the exception to recover its stack trace, so type and message are checked, not the instance.
        assertThat(thrown).isInstanceOf(IOException::class.java)
        assertThat(thrown).hasMessageThat().isEqualTo("No network")
    }
}
