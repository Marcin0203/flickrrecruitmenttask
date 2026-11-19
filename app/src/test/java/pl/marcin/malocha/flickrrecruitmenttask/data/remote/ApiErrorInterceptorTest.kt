package pl.marcin.malocha.flickrrecruitmenttask.data.remote

import com.google.common.truth.Truth.assertThat
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Before
import org.junit.Test

/** Tests [ApiErrorInterceptor] in a real [OkHttpClient] call against a [MockWebServer]. */
class ApiErrorInterceptorTest {

    private lateinit var server: MockWebServer
    private val client = OkHttpClient.Builder()
        .addInterceptor(ApiErrorInterceptor())
        .build()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.close()
    }

    private fun call() = runCatching {
        client.newCall(Request.Builder().url(server.url("/")).build()).execute().use { it.body.string() }
    }

    @Test
    fun interceptTest_SuccessfulResponse_IsPassedOn() {
        server.enqueue(MockResponse(code = 200, body = "ok"))

        assertThat(call().getOrNull()).isEqualTo("ok")
    }

    @Test
    fun interceptTest_ClientError_ThrowsApiExceptionWithCode() {
        server.enqueue(MockResponse(code = 404))

        val exception = call().exceptionOrNull()

        assertThat(exception).isInstanceOf(ApiException::class.java)
        assertThat((exception as ApiException).code).isEqualTo(404)
    }

    @Test
    fun interceptTest_ServerError_ThrowsApiExceptionWithCode() {
        server.enqueue(MockResponse(code = 500))

        val exception = call().exceptionOrNull()

        assertThat(exception).isInstanceOf(ApiException::class.java)
        assertThat((exception as ApiException).code).isEqualTo(500)
    }
}
