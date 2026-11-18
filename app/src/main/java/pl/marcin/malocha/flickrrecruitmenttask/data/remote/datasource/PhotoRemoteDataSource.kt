package pl.marcin.malocha.flickrrecruitmenttask.data.remote.datasource

import pl.marcin.malocha.flickrrecruitmenttask.data.remote.dto.PublicPhotosFeedDto

/** Remote source of public Flickr photos. */
interface PhotoRemoteDataSource {

    /**
     * Returns the raw feed of the most recent public photos matching all [tags].
     *
     * @param tags tags to search for, for example `["cat", "dog"]`.
     * @throws Exception if the request fails, for example with no network or an HTTP error.
     */
    suspend fun getPublicPhotos(tags: List<String>): PublicPhotosFeedDto
}
