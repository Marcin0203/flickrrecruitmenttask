package pl.marcin.malocha.flickrrecruitmenttask.di.providers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

abstract class TestDispatchers(val testDispatchers: TestDispatcher) : DispatcherProvider {
    override val main: CoroutineDispatcher
        get() = testDispatchers
    override val io: CoroutineDispatcher
        get() = testDispatchers
    override val default: CoroutineDispatcher
        get() = testDispatchers
}

class StandardTestDispatcher(scheduler: TestCoroutineScheduler? = null) :
    TestDispatchers(StandardTestDispatcher(scheduler = scheduler))

@OptIn(ExperimentalCoroutinesApi::class)
class UnconfinedTestDispatcher(scheduler: TestCoroutineScheduler? = null) :
    TestDispatchers(UnconfinedTestDispatcher(scheduler = scheduler))
