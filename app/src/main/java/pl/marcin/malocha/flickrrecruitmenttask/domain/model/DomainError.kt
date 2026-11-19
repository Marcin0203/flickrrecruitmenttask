package pl.marcin.malocha.flickrrecruitmenttask.domain.model

/**
 * Reasons why loading data failed, independent of the network library.
 *
 * Repositories return these as the failure of a `Result`. New kinds of errors, for example business rules,
 * are added as new subtypes, so every `when` over them has to handle the new case.
 */
sealed class DomainError : Exception() {

    /** The API answered with an HTTP error, for example `404` or `500`. */
    data class Api(val code: Int) : DomainError()

    /** The API could not be reached, for example no connection or a timeout. */
    data object Network : DomainError()

    /** Any other failure, for example a response that could not be parsed. */
    data class Unknown(override val cause: Throwable) : DomainError()
}
