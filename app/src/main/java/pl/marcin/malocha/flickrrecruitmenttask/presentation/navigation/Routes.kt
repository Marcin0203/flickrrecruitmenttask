package pl.marcin.malocha.flickrrecruitmenttask.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Full-screen view of one photo.
 *
 * @property imageUrl URL of the large image.
 * @property title title of the photo, can be empty.
 */
@Serializable
data class PhotoDetailRoute(
    val imageUrl: String,
    val title: String,
)
