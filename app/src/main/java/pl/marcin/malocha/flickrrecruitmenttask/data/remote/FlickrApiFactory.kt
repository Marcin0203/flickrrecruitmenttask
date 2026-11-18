package pl.marcin.malocha.flickrrecruitmenttask.data.remote

import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Builds [FlickrApi]. Kept outside the Hilt module, so tests can point the same setup at a mock server.
 */
object FlickrApiFactory {

    val BASE_URL: HttpUrl = "https://api.flickr.com/".toHttpUrl()

    private val json = Json { ignoreUnknownKeys = true }

    fun create(
        client: OkHttpClient,
        baseUrl: HttpUrl = BASE_URL,
    ): FlickrApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(FlickrApi::class.java)
}
