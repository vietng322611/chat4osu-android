package com.chat4osu.di

import com.chat4osu.osuIRC.OsuSocket
import com.chat4osu.services.ChatService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    @Singleton
    fun provideChatService(): ChatService {
        return ChatService(OsuSocket())
    }
}