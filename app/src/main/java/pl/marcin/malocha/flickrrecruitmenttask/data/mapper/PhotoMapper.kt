package pl.marcin.malocha.flickrrecruitmenttask.data.mapper

import pl.marcin.malocha.flickrrecruitmenttask.data.remote.dto.FeedPhotoDto
import pl.marcin.malocha.flickrrecruitmenttask.data.remote.dto.PublicPhotosFeedDto
import pl.marcin.malocha.flickrrecruitmenttask.domain.model.Photo

/**
 * Size suffix of the small image in a Flickr image URL, right before the file extension,
 * for example `_m` in `.../123_abc_m.jpg`.
 */
private val SMALL_SIZE_SUFFIX = Regex("_m(?=\\.[a-z]+$)")

/** Size suffix of the large (1024 px) image. */
private const val LARGE_SIZE_SUFFIX = "_b"

fun PublicPhotosFeedDto.toDomain(): List<Photo> = items.map { it.toDomain() }

fun FeedPhotoDto.toDomain(): Photo = Photo(
    id = link,
    title = title,
    thumbnailUrl = media.small,
    // If the URL has no small size suffix, it is left unchanged, so the full-screen view still shows the photo.
    fullSizeUrl = media.small.replace(SMALL_SIZE_SUFFIX, LARGE_SIZE_SUFFIX),
)
