package pl.marcin.malocha.flickrrecruitmenttask.domain.repository

import pl.marcin.malocha.flickrrecruitmenttask.domain.model.DomainError
import pl.marcin.malocha.flickrrecruitmenttask.domain.model.Photo

/** Source of public Flickr photos for the rest of the app. */
interface PhotoRepository {

    /**
     * Searches public photos matching all [tags].
     *
     * @param tags tags to search for, for example `["cat", "dog"]`.
     * @return the matching photos, or a failure with a [DomainError] if they could not be loaded.
     */
    suspend fun searchPhotos(tags: List<String>): Result<List<Photo>>
}
