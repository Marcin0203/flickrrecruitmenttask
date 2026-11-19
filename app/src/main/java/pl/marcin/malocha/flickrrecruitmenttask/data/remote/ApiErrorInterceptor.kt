package pl.marcin.malocha.flickrrecruitmenttask.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Turns every HTTP error response (4xx, 5xx) into an [ApiException] with its status code,
 * so the rest of the app can tell "the server answered with an error" apart from other failures.
 */
class ApiErrorInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (!response.isSuccessful) {
            // The body is not read, so the connection has to be released here.
            response.close()
            throw ApiException(code = response.code)
        }
        return response
    }
}

/**
 * The API answered with an HTTP error.
 *
 * It extends [IOException], because OkHttp expects interceptors to throw only `IOException`s.
 *
 * @property code HTTP status code, for example `404` or `500`.
 */
class ApiException(val code: Int) : IOException("HTTP $code")
