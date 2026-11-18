package pl.marcin.malocha.flickrrecruitmenttask.presentation.screen.search

import pl.marcin.malocha.flickrrecruitmenttask.domain.model.Photo

/**
 * @property query text typed into the search field.
 * @property photos photos found by the last successful search.
 * @property isLoading a search is in progress.
 * @property isError the last search failed (for example no network).
 * @property hasSearched at least one search has finished, so empty [photos] means "no results", not "not searched yet".
 */
data class SearchUiState(
    val query: String = "",
    val photos: List<Photo> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val hasSearched: Boolean = false,
) {
    val isSearchEnabled: Boolean
        get() = query.isNotBlank()

    /** The last search finished without error and found no photos. */
    val hasNoResults: Boolean
        get() = hasSearched && !isLoading && !isError && photos.isEmpty()
}

sealed interface SearchEvent {
    data class QueryChanged(val query: String) : SearchEvent
    data object SearchClicked : SearchEvent
}

sealed interface SearchAction {
    data object HideKeyboard : SearchAction
}
