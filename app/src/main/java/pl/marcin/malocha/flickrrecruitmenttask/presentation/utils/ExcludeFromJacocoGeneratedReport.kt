package pl.marcin.malocha.flickrrecruitmenttask.presentation.utils

/**
 * Excludes the annotated function or class from the JaCoCo coverage report.
 *
 * JaCoCo skips code marked with an annotation whose name contains "Generated" and that is kept in the class file.
 * Use it for code that is not meant to be tested, such as Compose previews.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class ExcludeFromJacocoGeneratedReport
