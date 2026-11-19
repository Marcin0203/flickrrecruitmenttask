package pl.marcin.malocha.flickrrecruitmenttask.domain.model

/**
 * A public Flickr photo, independent of the API response format.
 *
 * @property id unique identifier of the photo (its Flickr page URL).
 * @property title title of the photo, can be empty.
 * @property thumbnailUrl URL of the small image shown in the list.
 * @property fullSizeUrl URL of the large image shown in the full-screen view.
 */
data class Photo(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val fullSizeUrl: String,
)
