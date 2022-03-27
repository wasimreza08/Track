package com.example.domain.di

import com.example.core.dispatcher.BaseDispatcherProvider
import com.example.core.dispatcher.MainDispatcherProvider
import com.example.domain.domain.repository.Repository
import com.example.domain.domain.usecase.GetVehiclesUseCase
import com.example.domain.domain.usecase.GetVehiclesUseCaseImpl
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object DomainModule {
    @Reusable
    @Provides
    fun provideDispatcher(): BaseDispatcherProvider {
        return MainDispatcherProvider()
    }

    @Reusable
    @Provides
    fun provideGetVehicleUseCase(
        repository: Repository,
        mainDispatcherProvider: BaseDispatcherProvider
    ): GetVehiclesUseCase {
        return GetVehiclesUseCaseImpl(repository, mainDispatcherProvider)
    }
}
