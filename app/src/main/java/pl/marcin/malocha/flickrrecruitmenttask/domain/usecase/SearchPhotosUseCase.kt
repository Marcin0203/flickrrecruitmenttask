package pl.marcin.malocha.flickrrecruitmenttask.domain.usecase

import pl.marcin.malocha.flickrrecruitmenttask.domain.model.Photo
import pl.marcin.malocha.flickrrecruitmenttask.domain.repository.PhotoRepository
import javax.inject.Inject

/** Commas and whitespace that separate tags in the search query, for example `cat, dog` or `cat dog`. */
private val TAG_SEPARATORS = Regex("[,\\s]+")

/** Searches public photos for the tags the user typed into the search field. */
class SearchPhotosUseCase @Inject constructor(
    private val repository: PhotoRepository,
) {

    /**
     * Splits [query] into tags and searches photos matching all of them.
     *
     * @param query text from the search field, for example `"cat, dog"`.
     * @return the matching photos, an empty list if [query] has no tags, or a failure with a
     * `DomainError` if they could not be loaded.
     */
    suspend operator fun invoke(query: String): Result<List<Photo>> {
        val tags = query.split(TAG_SEPARATORS)
            .filter { it.isNotEmpty() }
            .distinct()
        // Without tags the feed returns the most recent photos of any kind, which is not a search result.
        if (tags.isEmpty()) return Result.success(emptyList())
        return repository.searchPhotos(tags)
    }
}
