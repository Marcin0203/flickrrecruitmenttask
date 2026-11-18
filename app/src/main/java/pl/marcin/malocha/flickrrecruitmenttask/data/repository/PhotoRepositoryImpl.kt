package pl.marcin.malocha.flickrrecruitmenttask.data.repository

import pl.marcin.malocha.flickrrecruitmenttask.data.mapper.toDomain
import pl.marcin.malocha.flickrrecruitmenttask.data.remote.datasource.PhotoRemoteDataSource
import pl.marcin.malocha.flickrrecruitmenttask.domain.model.Photo
import pl.marcin.malocha.flickrrecruitmenttask.domain.repository.PhotoRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/** [PhotoRepository] that loads photos from the [PhotoRemoteDataSource]. */
class PhotoRepositoryImpl @Inject constructor(
    private val remoteDataSource: PhotoRemoteDataSource,
) : PhotoRepository {

    override suspend fun searchPhotos(tags: List<String>): Result<List<Photo>> =
        try {
            Result.success(remoteDataSource.getPublicPhotos(tags).toDomain())
        } catch (e: CancellationException) {
            // Cancellation must reach the caller, otherwise a cancelled search would be reported as an error.
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}
