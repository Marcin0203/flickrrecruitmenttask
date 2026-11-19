package pl.marcin.malocha.flickrrecruitmenttask.presentation.model

/** Why loading data failed, as shown in the UI. Each case has its own message. */
sealed interface ErrorUi {

    /** The API answered with an HTTP error. The [code] is shown in the message. */
    data class Api(val code: Int) : ErrorUi

    /** The API could not be reached, for example no connection. */
    data object Network : ErrorUi

    /** Any other failure. */
    data object Unknown : ErrorUi
}
