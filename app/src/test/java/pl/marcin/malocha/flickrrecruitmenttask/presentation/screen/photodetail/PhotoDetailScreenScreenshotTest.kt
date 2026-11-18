package pl.marcin.malocha.flickrrecruitmenttask.presentation.screen.photodetail

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test
import pl.marcin.malocha.flickrrecruitmenttask.utils.snapshotLightAndDark

/**
 * Paparazzi screenshot tests for [PhotoDetailScreen], rendered through the stateless [PhotoDetailScreenLayout].
 * Images are not loaded in Paparazzi, so only the top bar is visible.
 */
class PhotoDetailScreenScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    @Test
    fun screenshotTest_WithTitle() = paparazzi.snapshotLightAndDark {
        PhotoDetailScreenLayout(
            state = PhotoDetailUiState(imageUrl = "https://live.staticflickr.com/65535/111_abc_b.jpg", title = "Sleepy cat"),
            onEvent = {},
        )
    }

    @Test
    fun screenshotTest_WithoutTitle() = paparazzi.snapshotLightAndDark {
        PhotoDetailScreenLayout(
            state = PhotoDetailUiState(imageUrl = "https://live.staticflickr.com/65535/111_abc_b.jpg", title = ""),
            onEvent = {},
        )
    }

    @Test
    fun screenshotTest_LongTitle() = paparazzi.snapshotLightAndDark {
        PhotoDetailScreenLayout(
            state = PhotoDetailUiState(
                imageUrl = "https://live.staticflickr.com/65535/111_abc_b.jpg",
                title = "A dog running on the beach on a sunny afternoon in summer",
            ),
            onEvent = {},
        )
    }
}
