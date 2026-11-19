package pl.marcin.malocha.flickrrecruitmenttask.presentation.mapper

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import pl.marcin.malocha.flickrrecruitmenttask.domain.model.Photo
import pl.marcin.malocha.flickrrecruitmenttask.presentation.model.PhotoUi

class PhotoUiMapperTest {

    @Test
    fun photoToUiTest_MapsAllFields() {
        val photo = Photo(
            id = "https://www.flickr.com/photos/user/111/",
            title = "Sleepy cat",
            thumbnailUrl = "https://live.staticflickr.com/65535/111_abc_m.jpg",
            fullSizeUrl = "https://live.staticflickr.com/65535/111_abc_b.jpg",
        )

        assertThat(photo.toUi()).isEqualTo(
            PhotoUi(
                id = "https://www.flickr.com/photos/user/111/",
                title = "Sleepy cat",
                thumbnailUrl = "https://live.staticflickr.com/65535/111_abc_m.jpg",
                fullSizeUrl = "https://live.staticflickr.com/65535/111_abc_b.jpg",
            ),
        )
    }
}
