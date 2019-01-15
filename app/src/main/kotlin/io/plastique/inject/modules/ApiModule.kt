package io.plastique.inject.modules

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.Reusable
import io.plastique.api.auth.AuthService
import io.plastique.api.collections.CollectionService
import io.plastique.api.comments.CommentService
import io.plastique.api.deviations.DeviationService
import io.plastique.api.feed.FeedElementDto
import io.plastique.api.feed.FeedElementTypes
import io.plastique.api.feed.FeedService
import io.plastique.api.gallery.GalleryService
import io.plastique.api.statuses.StatusService
import io.plastique.api.users.StatusDto
import io.plastique.api.users.UserService
import io.plastique.api.watch.WatchService
import io.plastique.core.adapters.InstantAdapter
import io.plastique.core.adapters.NullIfDeletedJsonAdapterFactory
import io.plastique.core.adapters.OffsetCursorAdapter
import io.plastique.core.adapters.StringCursorAdapter
import io.plastique.core.adapters.ZonedDateTimeAdapter
import io.plastique.core.client.ApiClient
import org.threeten.bp.format.DateTimeFormatter

@Module
object ApiModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideAuthService(apiClient: ApiClient): AuthService {
        return apiClient.getService(AuthService::class.java)
    }

    @Provides
    @Reusable
    @JvmStatic
    fun provideCommentService(apiClient: ApiClient): CommentService {
        return apiClient.getService(CommentService::class.java)
    }

    @Provides
    @Reusable
    @JvmStatic
    fun provideCollectionService(apiClient: ApiClient): CollectionService {
        return apiClient.getService(CollectionService::class.java)
    }

    @Provides
    @Reusable
    @JvmStatic
    fun provideDeviationService(apiClient: ApiClient): DeviationService {
        return apiClient.getService(DeviationService::class.java)
    }

    @Provides
    @Reusable
    @JvmStatic
    fun provideFeedService(apiClient: ApiClient): FeedService {
        return apiClient.getService(FeedService::class.java)
    }

    @Provides
    @Reusable
    @JvmStatic
    fun provideGalleryService(apiClient: ApiClient): GalleryService {
        return apiClient.getService(GalleryService::class.java)
    }

    @Provides
    @Reusable
    @JvmStatic
    fun provideStatusService(apiClient: ApiClient): StatusService {
        return apiClient.getService(StatusService::class.java)
    }

    @Provides
    @Reusable
    @JvmStatic
    fun provideUserService(apiClient: ApiClient): UserService {
        return apiClient.getService(UserService::class.java)
    }

    @Provides
    @Reusable
    @JvmStatic
    fun provideWatchService(apiClient: ApiClient): WatchService {
        return apiClient.getService(WatchService::class.java)
    }

    @Provides
    @Reusable
    @JvmStatic
    fun provideMoshi(): Moshi {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
        return Moshi.Builder()
                .add(InstantAdapter())
                .add(ZonedDateTimeAdapter(dateTimeFormatter))
                .add(OffsetCursorAdapter())
                .add(StringCursorAdapter())
                .add(PolymorphicJsonAdapterFactory.of(FeedElementDto::class.java, "type")
                        .withSubtype(FeedElementDto.CollectionUpdate::class.java, FeedElementTypes.COLLECTION_UPDATE)
                        .withSubtype(FeedElementDto.DeviationSubmitted::class.java, FeedElementTypes.DEVIATION_SUBMITTED)
                        .withSubtype(FeedElementDto.JournalSubmitted::class.java, FeedElementTypes.JOURNAL_SUBMITTED)
                        .withSubtype(FeedElementDto.StatusUpdate::class.java, FeedElementTypes.STATUS_UPDATE)
                        .withSubtype(FeedElementDto.UsernameChange::class.java, FeedElementTypes.USERNAME_CHANGE)
                        .withDefaultValue(FeedElementDto.Unknown))
                .add(PolymorphicJsonAdapterFactory.of(StatusDto.EmbeddedItem::class.java, "type")
                        .withSubtype(StatusDto.EmbeddedItem.SharedDeviation::class.java, StatusDto.EmbeddedItem.TYPE_DEVIATION)
                        .withSubtype(StatusDto.EmbeddedItem.SharedStatus::class.java, StatusDto.EmbeddedItem.TYPE_STATUS)
                        .withDefaultValue(StatusDto.EmbeddedItem.Unknown))
                .add(NullIfDeletedJsonAdapterFactory())
                .build()
    }
}
