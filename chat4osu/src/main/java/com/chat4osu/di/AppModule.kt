package com.chat4osu.di

import com.chat4osu.osu.AppSocket
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    var socket = AppSocket()
}