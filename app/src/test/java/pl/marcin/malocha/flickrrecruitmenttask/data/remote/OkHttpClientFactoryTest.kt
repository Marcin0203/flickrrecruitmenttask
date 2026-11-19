package pl.marcin.malocha.flickrrecruitmenttask.data.remote

import com.google.common.truth.Truth.assertThat
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Test

class OkHttpClientFactoryTest {

    @Test
    fun createTest_Debug_LogsRequestsWithBody() {
        val client = OkHttpClientFactory.create(isDebug = true)

        val loggingInterceptor = client.interceptors.filterIsInstance<HttpLoggingInterceptor>().single()
        assertThat(loggingInterceptor.level).isEqualTo(HttpLoggingInterceptor.Level.BODY)
    }

    @Test
    fun createTest_Release_DoesNotLog() {
        val client = OkHttpClientFactory.create(isDebug = false)

        assertThat(client.interceptors.filterIsInstance<HttpLoggingInterceptor>()).isEmpty()
    }
}
