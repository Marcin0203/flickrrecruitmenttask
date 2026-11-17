package pl.marcin.malocha.flickrrecruitmenttask.presentation.search

import dagger.hilt.android.lifecycle.HiltViewModel
import pl.marcin.malocha.flickrrecruitmenttask.presentation.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor() :
    BaseViewModel<SearchUiState, SearchEvent, SearchAction>(SearchUiState()) {

    override fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.QueryChanged -> setState { copy(query = event.query) }
            SearchEvent.SearchClicked -> onSearchClicked()
        }
    }

    private fun onSearchClicked() {
        if (!currentState.isSearchEnabled) return
        sendAction(SearchAction.HideKeyboard)
    }
}
