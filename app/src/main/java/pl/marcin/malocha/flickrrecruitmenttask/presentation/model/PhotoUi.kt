package pl.marcin.malocha.flickrrecruitmenttask.presentation.model

/**
 * A photo as shown in the UI, mapped from the domain `Photo`.
 *
 * @property id unique identifier of the photo, used as the key of its grid item.
 * @property title title of the photo, can be empty.
 * @property thumbnailUrl URL of the small image shown in the grid.
 * @property fullSizeUrl URL of the large image shown in the full-screen view.
 * @property hasTitle the photo has a non-blank [title], otherwise the UI shows a fallback text.
 */
data class PhotoUi(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val fullSizeUrl: String,
) {
    val hasTitle: Boolean
        get() = title.isNotBlank()
}
