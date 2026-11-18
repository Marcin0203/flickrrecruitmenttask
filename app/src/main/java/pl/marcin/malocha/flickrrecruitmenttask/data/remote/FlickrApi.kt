package pl.marcin.malocha.flickrrecruitmenttask.data.remote

import pl.marcin.malocha.flickrrecruitmenttask.data.remote.dto.PublicPhotosFeedDto
import retrofit2.http.GET
import retrofit2.http.Query

/** Flickr public photos feed. It needs no API key. */
interface FlickrApi {

    /**
     * Returns the 20 most recent public photos matching [tags].
     *
     * @param tags comma-separated list of tags, for example `cat,dog`.
     */
    @GET("services/feeds/photos_public.gne?format=json&nojsoncallback=1")
    suspend fun getPublicPhotos(@Query("tags") tags: String): PublicPhotosFeedDto
}
