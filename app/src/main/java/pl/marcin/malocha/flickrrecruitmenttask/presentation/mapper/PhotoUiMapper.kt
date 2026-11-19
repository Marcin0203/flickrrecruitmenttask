package pl.marcin.malocha.flickrrecruitmenttask.presentation.mapper

import pl.marcin.malocha.flickrrecruitmenttask.domain.model.Photo
import pl.marcin.malocha.flickrrecruitmenttask.presentation.model.PhotoUi

fun Photo.toUi(): PhotoUi = PhotoUi(
    id = id,
    title = title,
    thumbnailUrl = thumbnailUrl,
    fullSizeUrl = fullSizeUrl,
)
