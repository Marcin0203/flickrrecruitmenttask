package pl.marcin.malocha.flickrrecruitmenttask.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import pl.marcin.malocha.flickrrecruitmenttask.data.remote.FlickrApi
import pl.marcin.malocha.flickrrecruitmenttask.data.remote.FlickrApiFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient()

    @Provides
    @Singleton
    fun provideFlickrApi(client: OkHttpClient): FlickrApi = FlickrApiFactory.create(client)
}
