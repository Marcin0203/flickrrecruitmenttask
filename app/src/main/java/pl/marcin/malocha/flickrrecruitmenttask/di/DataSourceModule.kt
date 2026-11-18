package pl.marcin.malocha.flickrrecruitmenttask.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.marcin.malocha.flickrrecruitmenttask.data.remote.datasource.PhotoRemoteDataSource
import pl.marcin.malocha.flickrrecruitmenttask.data.remote.datasource.PhotoRemoteDataSourceImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class DataSourceModule {
    @Binds
    @Singleton
    abstract fun bindPhotoRemoteDataSource(photoRemoteDataSourceImpl: PhotoRemoteDataSourceImpl): PhotoRemoteDataSource
}
