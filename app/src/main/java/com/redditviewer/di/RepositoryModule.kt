package com.redditviewer.di

import com.redditviewer.data.repository.AuthRepositoryImpl
import com.redditviewer.data.repository.RedditRepositoryImpl
import com.redditviewer.data.repository.TranslationRepositoryImpl
import com.redditviewer.domain.repository.AuthRepository
import com.redditviewer.domain.repository.RedditRepository
import com.redditviewer.domain.repository.TranslationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRedditRepository(
        redditRepositoryImpl: RedditRepositoryImpl
    ): RedditRepository

    @Binds
    @Singleton
    abstract fun bindTranslationRepository(
        translationRepositoryImpl: TranslationRepositoryImpl
    ): TranslationRepository
} 