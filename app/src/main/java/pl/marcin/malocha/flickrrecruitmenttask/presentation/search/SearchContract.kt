package pl.marcin.malocha.flickrrecruitmenttask.presentation.search

data class SearchUiState(
    val query: String = "",
) {
    val isSearchEnabled: Boolean
        get() = query.isNotBlank()
}

sealed interface SearchEvent {
    data class QueryChanged(val query: String) : SearchEvent
    data object SearchClicked : SearchEvent
}

sealed interface SearchAction {
    data object HideKeyboard : SearchAction
}
