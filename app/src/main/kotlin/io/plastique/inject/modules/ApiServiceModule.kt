package io.plastique.inject.modules

import dagger.Module
import dagger.Provides
import dagger.Reusable
import io.plastique.api.auth.AuthService
import io.plastique.api.collections.CollectionService
import io.plastique.api.comments.CommentService
import io.plastique.api.deviations.DeviationService
import io.plastique.api.feed.FeedService
import io.plastique.api.gallery.GalleryService
import io.plastique.api.messages.MessageService
import io.plastique.api.statuses.StatusService
import io.plastique.api.users.UserService
import io.plastique.api.watch.WatchService
import io.plastique.core.client.ApiClient

@Module
object ApiServiceModule {
    @Provides
    @Reusable
    fun provideAuthService(apiClient: ApiClient): AuthService = apiClient.createService()

    @Provides
    @Reusable
    fun provideCommentService(apiClient: ApiClient): CommentService = apiClient.createService()

    @Provides
    @Reusable
    fun provideCollectionService(apiClient: ApiClient): CollectionService = apiClient.createService()

    @Provides
    @Reusable
    fun provideDeviationService(apiClient: ApiClient): DeviationService = apiClient.createService()

    @Provides
    @Reusable
    fun provideFeedService(apiClient: ApiClient): FeedService = apiClient.createService()

    @Provides
    @Reusable
    fun provideGalleryService(apiClient: ApiClient): GalleryService = apiClient.createService()

    @Provides
    @Reusable
    fun provideMessageService(apiClient: ApiClient): MessageService = apiClient.createService()

    @Provides
    @Reusable
    fun provideStatusService(apiClient: ApiClient): StatusService = apiClient.createService()

    @Provides
    @Reusable
    fun provideUserService(apiClient: ApiClient): UserService = apiClient.createService()

    @Provides
    @Reusable
    fun provideWatchService(apiClient: ApiClient): WatchService = apiClient.createService()
}
