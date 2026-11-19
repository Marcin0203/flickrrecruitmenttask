package pl.marcin.malocha.flickrrecruitmenttask.data.mapper

import pl.marcin.malocha.flickrrecruitmenttask.data.remote.ApiException
import pl.marcin.malocha.flickrrecruitmenttask.domain.model.DomainError
import java.io.IOException

/** Maps an exception from the data layer to a [DomainError]. */
fun Throwable.toDomainError(): DomainError = when (this) {
    // ApiException is an IOException, so it has to be checked first.
    is ApiException -> DomainError.Api(code = code)
    is IOException -> DomainError.Network
    else -> DomainError.Unknown(cause = this)
}
