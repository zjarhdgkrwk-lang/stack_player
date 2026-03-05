package com.stack.data.di

import com.stack.data.scanner.ScanManager
import com.stack.domain.repository.ScanService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ScannerModule {

    @Binds
    @Singleton
    abstract fun bindScanService(impl: ScanManager): ScanService
}
