package pl.marcin.malocha.flickrrecruitmenttask.data.remote.datasource

import kotlinx.coroutines.withContext
import pl.marcin.malocha.flickrrecruitmenttask.data.remote.FlickrApi
import pl.marcin.malocha.flickrrecruitmenttask.data.remote.dto.PublicPhotosFeedDto
import pl.marcin.malocha.flickrrecruitmenttask.di.providers.DispatcherProvider
import javax.inject.Inject

/** [PhotoRemoteDataSource] backed by the Flickr public photos feed ([FlickrApi]). */
class PhotoRemoteDataSourceImpl @Inject constructor(
    private val api: FlickrApi,
    private val dispatcherProvider: DispatcherProvider,
) : PhotoRemoteDataSource {

    override suspend fun getPublicPhotos(tags: List<String>): PublicPhotosFeedDto =
        withContext(dispatcherProvider.io) {
            // The feed expects the tags as one comma-separated parameter.
            api.getPublicPhotos(tags.joinToString(TAG_SEPARATOR))
        }

    private companion object {
        const val TAG_SEPARATOR = ","
    }
}
