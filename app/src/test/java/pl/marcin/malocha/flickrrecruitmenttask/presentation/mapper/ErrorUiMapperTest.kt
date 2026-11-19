package pl.marcin.malocha.flickrrecruitmenttask.presentation.mapper

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import pl.marcin.malocha.flickrrecruitmenttask.domain.model.DomainError
import pl.marcin.malocha.flickrrecruitmenttask.presentation.model.ErrorUi

class ErrorUiMapperTest {

    @Test
    fun toErrorUiTest_ApiError_IsApiWithCode() {
        assertThat(DomainError.Api(code = 404).toErrorUi()).isEqualTo(ErrorUi.Api(code = 404))
    }

    @Test
    fun toErrorUiTest_NetworkError_IsNetwork() {
        assertThat(DomainError.Network.toErrorUi()).isEqualTo(ErrorUi.Network)
    }

    @Test
    fun toErrorUiTest_UnknownError_IsUnknown() {
        assertThat(DomainError.Unknown(cause = IllegalStateException()).toErrorUi()).isEqualTo(ErrorUi.Unknown)
    }

    @Test
    fun toErrorUiTest_NotDomainError_IsUnknown() {
        assertThat(IllegalStateException().toErrorUi()).isEqualTo(ErrorUi.Unknown)
    }
}
