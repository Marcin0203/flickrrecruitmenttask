package pl.marcin.malocha.flickrrecruitmenttask.utils

import androidx.compose.runtime.Composable
import app.cash.paparazzi.Paparazzi
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

/** App theme with dynamic color off, so snapshots do not depend on the wallpaper or API level. */
@Composable
private fun SnapshotTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    FlickrRecruitmentTaskTheme(darkTheme = darkTheme, dynamicColor = false, content = content)
}
