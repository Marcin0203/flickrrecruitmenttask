package pl.marcin.malocha.flickrrecruitmenttask.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.marcin.malocha.flickrrecruitmenttask.di.providers.DispatcherProvider

/**
 * Base class for MVI view models.
 *
 * - [S] UI state: a data class exposed as [StateFlow], always holds the latest value.
 * - [E] events: the only input from the view, passed to [onEvent].
 * - [A] actions: one-off messages for the view (e.g. hide keyboard, navigation) exposed as [SharedFlow] without replay.
 *
 * Coroutines run on [dispatchers], so tests can swap them for test dispatchers.
 */
abstract class BaseViewModel<S, E, A>(
    initialState: S,
    protected val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _actions = MutableSharedFlow<A>()
    val actions: SharedFlow<A> = _actions.asSharedFlow()

    abstract fun onEvent(event: E)

    protected val currentState: S
        get() = _state.value

    protected fun setState(reducer: S.() -> S) {
        _state.update(reducer)
    }

    protected fun sendAction(action: A) {
        viewModelScope.launch(dispatchers.main) { _actions.emit(action) }
    }
}
