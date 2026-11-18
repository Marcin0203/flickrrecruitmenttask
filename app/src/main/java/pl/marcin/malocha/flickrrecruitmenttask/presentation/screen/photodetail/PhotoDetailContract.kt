package pl.marcin.malocha.flickrrecruitmenttask.presentation.screen.photodetail

/**
 * @property imageUrl URL of the large image.
 * @property title title of the photo, can be empty.
 */
data class PhotoDetailUiState(
    val imageUrl: String,
    val title: String,
) {
    val hasTitle: Boolean
        get() = title.isNotBlank()
}

sealed interface PhotoDetailEvent {
    data object BackClicked : PhotoDetailEvent
}

sealed interface PhotoDetailAction {
    data object NavigateBack : PhotoDetailAction
}
