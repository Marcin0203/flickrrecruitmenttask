package pl.marcin.malocha.flickrrecruitmenttask.presentation.mapper

import pl.marcin.malocha.flickrrecruitmenttask.domain.model.DomainError
import pl.marcin.malocha.flickrrecruitmenttask.presentation.model.ErrorUi

/** Maps the failure of a domain `Result` to an [ErrorUi]. Anything that is not a [DomainError] is [ErrorUi.Unknown]. */
fun Throwable.toErrorUi(): ErrorUi = when (this) {
    is DomainError.Api -> ErrorUi.Api(code = code)
    DomainError.Network -> ErrorUi.Network
    // DomainError.Unknown and any exception that is not a DomainError.
    else -> ErrorUi.Unknown
}
