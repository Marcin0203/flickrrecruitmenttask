package pl.marcin.malocha.flickrrecruitmenttask.presentation.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PhotoUiTest {

    private fun photo(title: String) = PhotoUi(
        id = "https://www.flickr.com/photos/user/111/",
        title = title,
        thumbnailUrl = "https://live.staticflickr.com/65535/111_abc_m.jpg",
        fullSizeUrl = "https://live.staticflickr.com/65535/111_abc_b.jpg",
    )

    @Test
    fun hasTitleTest_Title_IsTrue() {
        assertThat(photo(title = "Sleepy cat").hasTitle).isTrue()
    }

    @Test
    fun hasTitleTest_EmptyTitle_IsFalse() {
        assertThat(photo(title = "").hasTitle).isFalse()
    }

    @Test
    fun hasTitleTest_BlankTitle_IsFalse() {
        assertThat(photo(title = "   ").hasTitle).isFalse()
    }
}
