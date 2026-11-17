package pl.marcin.malocha.flickrrecruitmenttask.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.marcin.malocha.flickrrecruitmenttask.di.providers.DefaultDispatchers
import pl.marcin.malocha.flickrrecruitmenttask.di.providers.DispatcherProvider
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class DispatcherProviderModule {
    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(defaultDispatchers: DefaultDispatchers): DispatcherProvider
}
