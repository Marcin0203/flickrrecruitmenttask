package pl.marcin.malocha.flickrrecruitmenttask.domain.model

/**
 * A public Flickr photo, independent of the API response format.
 *
 * @property id unique identifier of the photo (its Flickr page URL).
 * @property title title of the photo, can be empty.
 * @property thumbnailUrl URL of the small image shown in the list.
 * @property fullSizeUrl URL of the large image shown in the full-screen view.
 * @property hasTitle the photo has a non-blank [title], otherwise the UI shows a fallback text.
 */
data class Photo(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val fullSizeUrl: String,
) {
    val hasTitle: Boolean
        get() = title.isNotBlank()
}
