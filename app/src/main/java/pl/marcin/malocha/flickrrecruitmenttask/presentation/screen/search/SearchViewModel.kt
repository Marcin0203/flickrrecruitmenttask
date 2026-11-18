package pl.marcin.malocha.flickrrecruitmenttask.presentation.screen.search

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pl.marcin.malocha.flickrrecruitmenttask.di.providers.DispatcherProvider
import pl.marcin.malocha.flickrrecruitmenttask.domain.usecase.SearchPhotosUseCase
import pl.marcin.malocha.flickrrecruitmenttask.presentation.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchPhotos: SearchPhotosUseCase,
    dispatchers: DispatcherProvider,
) : BaseViewModel<SearchUiState, SearchEvent, SearchAction>(SearchUiState(), dispatchers) {

    private var searchJob: Job? = null

    override fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.QueryChanged -> setState { copy(query = event.query) }
            SearchEvent.SearchClicked -> onSearchClicked()
        }
    }

    private fun onSearchClicked() {
        if (!currentState.isSearchEnabled) return
        sendAction(SearchAction.HideKeyboard)
        search(currentState.query)
    }

    private fun search(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch(dispatchers.main) {
            setState { copy(isLoading = true, isError = false) }
            searchPhotos(query)
                .onSuccess { photos ->
                    setState { copy(photos = photos, isLoading = false, hasSearched = true) }
                }
                .onFailure {
                    setState { copy(photos = emptyList(), isLoading = false, isError = true, hasSearched = true) }
                }
        }
    }
}
