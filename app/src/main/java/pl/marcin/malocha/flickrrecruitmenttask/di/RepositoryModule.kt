package pl.marcin.malocha.flickrrecruitmenttask.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.marcin.malocha.flickrrecruitmenttask.data.repository.PhotoRepositoryImpl
import pl.marcin.malocha.flickrrecruitmenttask.domain.repository.PhotoRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindPhotoRepository(photoRepositoryImpl: PhotoRepositoryImpl): PhotoRepository
}
