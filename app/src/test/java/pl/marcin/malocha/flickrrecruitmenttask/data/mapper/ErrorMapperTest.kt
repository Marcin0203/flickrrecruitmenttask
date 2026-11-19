package pl.marcin.malocha.flickrrecruitmenttask.data.mapper

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import pl.marcin.malocha.flickrrecruitmenttask.data.remote.ApiException
import pl.marcin.malocha.flickrrecruitmenttask.domain.model.DomainError
import java.io.IOException

class ErrorMapperTest {

    @Test
    fun toDomainErrorTest_ApiException_IsApiWithCode() {
        assertThat(ApiException(code = 503).toDomainError()).isEqualTo(DomainError.Api(code = 503))
    }

    @Test
    fun toDomainErrorTest_IOException_IsNetwork() {
        assertThat(IOException("Timeout").toDomainError()).isEqualTo(DomainError.Network)
    }

    @Test
    fun toDomainErrorTest_OtherException_IsUnknownWithCause() {
        val exception = IllegalStateException("Broken JSON")

        assertThat(exception.toDomainError()).isEqualTo(DomainError.Unknown(cause = exception))
    }
}
