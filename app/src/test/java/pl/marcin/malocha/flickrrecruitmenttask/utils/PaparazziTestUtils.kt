package pl.marcin.malocha.flickrrecruitmenttask.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import app.cash.paparazzi.Paparazzi
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePainter
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import pl.marcin.malocha.flickrrecruitmenttask.presentation.theme.FlickrRecruitmentTaskTheme

/**
 * Takes two Paparazzi snapshots of [content], named `light` and `dark`, each wrapped in the app theme.
 *
 * One test per UI state covers both themes, so screen tests only describe what to render.
 */
fun Paparazzi.snapshotLightAndDark(content: @Composable () -> Unit) {
    snapshot(name = "light") { SnapshotTheme(darkTheme = false, content = content) }
    snapshot(name = "dark") { SnapshotTheme(darkTheme = true, content = content) }
}

/**
 * App theme with dynamic color off, so snapshots do not depend on the wallpaper or API level.
 *
 * Every Coil image stays in the loading state. Coil's default preview handler would really run the request,
 * so without this the snapshots would depend on the network.
 */
@OptIn(ExperimentalCoilApi::class)
@Composable
private fun SnapshotTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAsyncImagePreviewHandler provides AsyncImagePreviewHandler { _, _ -> AsyncImagePainter.State.Loading(null) },
    ) {
        FlickrRecruitmentTaskTheme(darkTheme = darkTheme, dynamicColor = false, content = content)
    }
}
