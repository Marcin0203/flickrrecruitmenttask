package pl.marcin.malocha.flickrrecruitmenttask.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response of the Flickr public photos feed. Only the fields the app uses are declared,
 * the rest are skipped by the parser (`ignoreUnknownKeys`).
 */
@Serializable
data class PublicPhotosFeedDto(
    val items: List<FeedPhotoDto>,
)

@Serializable
data class FeedPhotoDto(
    val title: String,
    val link: String,
    val media: FeedPhotoMediaDto,
)

@Serializable
data class FeedPhotoMediaDto(
    /** URL of the small (about 240 px) image, ending with the `_m` size suffix. */
    @SerialName("m") val small: String,
)
