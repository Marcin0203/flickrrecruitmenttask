package pl.marcin.malocha.flickrrecruitmenttask.presentation.screen.photodetail

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import pl.marcin.malocha.flickrrecruitmenttask.di.providers.DispatcherProvider
import pl.marcin.malocha.flickrrecruitmenttask.presentation.base.BaseViewModel
import pl.marcin.malocha.flickrrecruitmenttask.presentation.navigation.PhotoDetailRoute

/** Gets the photo from [route] through Hilt assisted injection, so tests create it with a plain constructor call. */
@HiltViewModel(assistedFactory = PhotoDetailViewModel.Factory::class)
class PhotoDetailViewModel @AssistedInject constructor(
    @Assisted route: PhotoDetailRoute,
    dispatchers: DispatcherProvider,
) : BaseViewModel<PhotoDetailUiState, PhotoDetailEvent, PhotoDetailAction>(
    PhotoDetailUiState(imageUrl = route.imageUrl, title = route.title),
    dispatchers,
) {

    @AssistedFactory
    interface Factory {
        fun create(route: PhotoDetailRoute): PhotoDetailViewModel
    }

    override fun onEvent(event: PhotoDetailEvent) {
        when (event) {
            PhotoDetailEvent.BackClicked -> sendAction(PhotoDetailAction.NavigateBack)
        }
    }
}
