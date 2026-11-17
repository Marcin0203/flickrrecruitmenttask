package pl.marcin.malocha.flickrrecruitmenttask.presentation.search

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test
import pl.marcin.malocha.flickrrecruitmenttask.utils.snapshotLightAndDark

/**
 * Paparazzi screenshot tests for [SearchScreen].
 *
 * Record goldens with `./gradlew recordPaparazziDebug`, verify with `./gradlew verifyPaparazziDebug`.
 */
class SearchScreenScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    @Test
    fun screenshotTest_EmptyQuery() = paparazzi.snapshotLightAndDark {
        SearchScreen(state = SearchUiState(query = ""), onEvent = {})
    }

    @Test
    fun screenshotTest_FilledQuery() = paparazzi.snapshotLightAndDark {
        SearchScreen(state = SearchUiState(query = "cat, dog"), onEvent = {})
    }
}
