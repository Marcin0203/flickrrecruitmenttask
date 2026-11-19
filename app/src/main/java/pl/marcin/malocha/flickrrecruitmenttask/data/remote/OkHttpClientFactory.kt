package pl.marcin.malocha.flickrrecruitmenttask.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Builds the [OkHttpClient] used by [FlickrApi]. Kept outside the Hilt module, so tests can check its setup.
 */
object OkHttpClientFactory {

    /**
     * [ApiErrorInterceptor] is added first, so it wraps the logging interceptor: an error response is logged in full
     * before it is turned into an [ApiException].
     *
     * @param isDebug in debug builds every request and response, with headers and body, is logged to Logcat.
     * Release builds do not log anything.
     */
    fun create(isDebug: Boolean): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(ApiErrorInterceptor())
        .apply {
            if (isDebug) {
                addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            }
        }
        .build()
}
