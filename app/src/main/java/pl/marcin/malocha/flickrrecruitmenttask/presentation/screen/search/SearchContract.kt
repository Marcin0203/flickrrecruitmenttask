package pl.marcin.malocha.flickrrecruitmenttask.presentation.screen.search

import pl.marcin.malocha.flickrrecruitmenttask.presentation.model.ErrorUi
import pl.marcin.malocha.flickrrecruitmenttask.presentation.model.PhotoUi

/**
 * @property query text typed into the search field.
 * @property photos photos found by the last successful search.
 * @property isLoading a search is in progress.
 * @property error why the last search failed, or `null` if it did not fail.
 * @property hasSearched at least one search has finished, so empty [photos] means "no results", not "not searched yet".
 */
data class SearchUiState(
    val query: String = "",
    val photos: List<PhotoUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: ErrorUi? = null,
    val hasSearched: Boolean = false,
) {
    val isSearchEnabled: Boolean
        get() = query.isNotBlank()

    /** The last search finished without error and found no photos. */
    val hasNoResults: Boolean
        get() = hasSearched && !isLoading && error == null && photos.isEmpty()
}

sealed interface SearchEvent {
    data class QueryChanged(val query: String) : SearchEvent
    data object SearchClicked : SearchEvent
    data class PhotoClicked(val photo: PhotoUi) : SearchEvent
}

sealed interface SearchAction {
    data object HideKeyboard : SearchAction
    data class NavigateToPhoto(val photo: PhotoUi) : SearchAction
}
