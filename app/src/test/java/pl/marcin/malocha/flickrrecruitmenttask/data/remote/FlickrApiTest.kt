package pl.marcin.malocha.flickrrecruitmenttask.data.remote

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Before
import org.junit.Test
import pl.marcin.malocha.flickrrecruitmenttask.data.remote.dto.FeedPhotoDto
import pl.marcin.malocha.flickrrecruitmenttask.data.remote.dto.FeedPhotoMediaDto
import retrofit2.HttpException

/**
 * Tests [FlickrApi] built by [FlickrApiFactory] against a [MockWebServer]:
 * the request it sends and how it parses feed responses (fixtures in `src/test/resources/feed`).
 */
class FlickrApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: FlickrApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = FlickrApiFactory.create(client = OkHttpClient(), baseUrl = server.url("/"))
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun baseUrlTest_PointsToFlickrApi() {
        assertThat(FlickrApiFactory.BASE_URL.toString()).isEqualTo("https://api.flickr.com/")
    }

    @Test
    fun getPublicPhotosTest_SendsTagsAndFeedParameters() = runTest {
        server.enqueue(jsonResponse("feed/empty_feed.json"))

        api.getPublicPhotos(tags = "cat,dog")

        val url = server.takeRequest().url
        assertThat(url.encodedPath).isEqualTo("/services/feeds/photos_public.gne")
        assertThat(url.queryParameter("format")).isEqualTo("json")
        assertThat(url.queryParameter("nojsoncallback")).isEqualTo("1")
        assertThat(url.queryParameter("tags")).isEqualTo("cat,dog")
    }

    @Test
    fun getPublicPhotosTest_ParsesFeedResponse() = runTest {
        server.enqueue(jsonResponse("feed/public_photos_feed.json"))

        val feed = api.getPublicPhotos(tags = "cat")

        assertThat(feed.items).containsExactly(
            FeedPhotoDto(
                title = "Sleepy cat",
                link = "https://www.flickr.com/photos/user/111/",
                media = FeedPhotoMediaDto(small = "https://live.staticflickr.com/65535/111_abc_m.jpg"),
            ),
            FeedPhotoDto(
                title = "",
                link = "https://www.flickr.com/photos/user/222/",
                media = FeedPhotoMediaDto(small = "https://live.staticflickr.com/65535/222_def_m.jpg"),
            ),
        ).inOrder()
    }

    @Test
    fun getPublicPhotosTest_EmptyFeed_ReturnsNoItems() = runTest {
        server.enqueue(jsonResponse("feed/empty_feed.json"))

        val feed = api.getPublicPhotos(tags = "nothingmatches")

        assertThat(feed.items).isEmpty()
    }

    @Test
    fun getPublicPhotosTest_HttpError_ThrowsHttpException() = runTest {
        server.enqueue(MockResponse(code = 500))

        val exception = runCatching { api.getPublicPhotos(tags = "cat") }.exceptionOrNull()

        assertThat(exception).isInstanceOf(HttpException::class.java)
        assertThat((exception as HttpException).code()).isEqualTo(500)
    }

    private fun jsonResponse(resourcePath: String) = MockResponse(
        code = 200,
        body = readResource(resourcePath),
    )

    private fun readResource(path: String): String =
        requireNotNull(javaClass.classLoader?.getResource(path)) { "Missing test resource: $path" }.readText()
}
