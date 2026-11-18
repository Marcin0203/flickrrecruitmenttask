package pl.marcin.malocha.flickrrecruitmenttask.presentation.search

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test
import pl.marcin.malocha.flickrrecruitmenttask.utils.snapshotLightAndDark

/**
 * Paparazzi screenshot tests for [SearchScreen], rendered through the stateless [SearchScreenLayout].
 *
 * Record goldens with `./gradlew recordPaparazziDebug`, verify with `./gradlew verifyPaparazziDebug`.
 */
class SearchScreenScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    @Test
    fun screenshotTest_EmptyQuery() = paparazzi.snapshotLightAndDark {
        SearchScreenLayout(state = SearchUiState(query = ""), onEvent = {})
    }

    @Test
    fun screenshotTest_FilledQuery() = paparazzi.snapshotLightAndDark {
        SearchScreenLayout(state = SearchUiState(query = "cat, dog"), onEvent = {})
    }

    @Test
    fun screenshotTest_Loading() = paparazzi.snapshotLightAndDark {
        SearchScreenLayout(state = SearchUiState(query = "cat, dog", isLoading = true), onEvent = {})
    }

    @Test
    fun screenshotTest_Error() = paparazzi.snapshotLightAndDark {
        SearchScreenLayout(
            state = SearchUiState(query = "cat, dog", isError = true, hasSearched = true),
            onEvent = {},
        )
    }

    @Test
    fun screenshotTest_NoResults() = paparazzi.snapshotLightAndDark {
        SearchScreenLayout(state = SearchUiState(query = "cat, dog", hasSearched = true), onEvent = {})
    }
}
