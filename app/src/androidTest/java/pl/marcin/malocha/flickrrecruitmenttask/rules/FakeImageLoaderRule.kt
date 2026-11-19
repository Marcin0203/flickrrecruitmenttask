package pl.marcin.malocha.flickrrecruitmenttask.rules

import androidx.test.platform.app.InstrumentationRegistry
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.test.FakeImage
import coil3.test.FakeImageLoaderEngine
import kotlinx.coroutines.CompletableDeferred
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * A JUnit rule that replaces Coil's singleton `ImageLoader` with a fake one for each test, so UI tests never load
 * images from the network.
 *
 * Every image stays in the loading state until [finishLoading] is called. Then it loads as a [FakeImage].
 */
class FakeImageLoaderRule : TestWatcher() {

    private val imagesLoaded = CompletableDeferred<Unit>()

    fun finishLoading() {
        imagesLoaded.complete(Unit)
    }

    override fun starting(description: Description) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val engine = FakeImageLoaderEngine.Builder()
            // Waits until the test lets images load, then passes the request on to the default image.
            .addInterceptor {
                imagesLoaded.await()
                null
            }
            .default(FakeImage())
            .build()
        SingletonImageLoader.setUnsafe(
            ImageLoader.Builder(context)
                .components { add(engine) }
                .build(),
        )
    }

    override fun finished(description: Description) {
        SingletonImageLoader.reset()
    }
}
