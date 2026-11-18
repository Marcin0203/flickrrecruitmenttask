package pl.marcin.malocha.flickrrecruitmenttask.data.mapper

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import pl.marcin.malocha.flickrrecruitmenttask.data.remote.dto.FeedPhotoDto
import pl.marcin.malocha.flickrrecruitmenttask.data.remote.dto.FeedPhotoMediaDto
import pl.marcin.malocha.flickrrecruitmenttask.data.remote.dto.PublicPhotosFeedDto

class PhotoMapperTest {

    @Test
    fun photoToDomainTest_MapsAllFields() {
        val photo = feedPhoto(
            title = "Sleepy cat",
            link = "https://www.flickr.com/photos/user/111/",
            smallUrl = "https://live.staticflickr.com/65535/111_abc_m.jpg",
        ).toDomain()

        assertThat(photo.id).isEqualTo("https://www.flickr.com/photos/user/111/")
        assertThat(photo.title).isEqualTo("Sleepy cat")
        assertThat(photo.thumbnailUrl).isEqualTo("https://live.staticflickr.com/65535/111_abc_m.jpg")
        assertThat(photo.fullSizeUrl).isEqualTo("https://live.staticflickr.com/65535/111_abc_b.jpg")
    }

    @Test
    fun photoToDomainTest_EmptyTitle_StaysEmpty() {
        val photo = feedPhoto(title = "").toDomain()

        assertThat(photo.title).isEmpty()
    }

    @Test
    fun photoToDomainTest_OtherExtension_SwapsSizeSuffix() {
        val photo = feedPhoto(smallUrl = "https://live.staticflickr.com/65535/111_abc_m.png").toDomain()

        assertThat(photo.fullSizeUrl).isEqualTo("https://live.staticflickr.com/65535/111_abc_b.png")
    }

    @Test
    fun photoToDomainTest_SuffixOnlySwappedBeforeExtension() {
        val photo = feedPhoto(smallUrl = "https://live.staticflickr.com/server_m/111_abc_m.jpg").toDomain()

        assertThat(photo.fullSizeUrl).isEqualTo("https://live.staticflickr.com/server_m/111_abc_b.jpg")
    }

    @Test
    fun photoToDomainTest_UrlWithoutSmallSuffix_KeptAsIs() {
        val photo = feedPhoto(smallUrl = "https://live.staticflickr.com/65535/111_abc.jpg").toDomain()

        assertThat(photo.fullSizeUrl).isEqualTo("https://live.staticflickr.com/65535/111_abc.jpg")
    }

    @Test
    fun feedToDomainTest_MapsAllItemsInOrder() {
        val feed = PublicPhotosFeedDto(
            items = listOf(
                feedPhoto(link = "https://www.flickr.com/photos/user/111/"),
                feedPhoto(link = "https://www.flickr.com/photos/user/222/"),
            ),
        )

        val photos = feed.toDomain()

        assertThat(photos.map { it.id }).containsExactly(
            "https://www.flickr.com/photos/user/111/",
            "https://www.flickr.com/photos/user/222/",
        ).inOrder()
    }

    @Test
    fun feedToDomainTest_EmptyFeed_ReturnsEmptyList() {
        val photos = PublicPhotosFeedDto(items = emptyList()).toDomain()

        assertThat(photos).isEmpty()
    }

    private fun feedPhoto(
        title: String = "Photo title",
        link: String = "https://www.flickr.com/photos/user/111/",
        smallUrl: String = "https://live.staticflickr.com/65535/111_abc_m.jpg",
    ) = FeedPhotoDto(
        title = title,
        link = link,
        media = FeedPhotoMediaDto(small = smallUrl),
    )
}
