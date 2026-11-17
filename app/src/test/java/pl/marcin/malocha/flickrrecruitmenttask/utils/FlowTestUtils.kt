package pl.marcin.malocha.flickrrecruitmenttask.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * Starts collecting [flow] into a list in [TestScope.backgroundScope] and returns that list.
 *
 * Collection starts immediately, so emissions from a [kotlinx.coroutines.flow.SharedFlow] without replay
 * (e.g. ViewModel actions) are not lost. The collector is cancelled automatically when the test ends.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> TestScope.collectToList(flow: Flow<T>): List<T> {
    val items = mutableListOf<T>()
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        flow.toList(items)
    }
    return items
}
