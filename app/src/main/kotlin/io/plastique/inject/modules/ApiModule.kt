package io.plastique.inject.modules

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import dagger.Binds
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.Reusable
import io.plastique.BuildConfig
import io.plastique.R
import io.plastique.api.ErrorResponseParserImpl
import io.plastique.api.NullIfDeletedJsonAdapterFactory
import io.plastique.api.auth.AuthService
import io.plastique.api.collections.CollectionService
import io.plastique.api.comments.CommentService
import io.plastique.api.common.ApiConstants
import io.plastique.api.deviations.DeviationService
import io.plastique.api.feed.FeedElementDto
import io.plastique.api.feed.FeedElementTypes
import io.plastique.api.feed.FeedService
import io.plastique.api.gallery.GalleryService
import io.plastique.api.messages.MessageService
import io.plastique.api.statuses.StatusDto
import io.plastique.api.statuses.StatusService
import io.plastique.api.users.UserService
import io.plastique.api.watch.WatchService
import io.plastique.core.client.AccessTokenProvider
import io.plastique.core.client.ApiClient
import io.plastique.core.client.ApiConfiguration
import io.plastique.core.client.ErrorResponseParser
import io.plastique.core.json.InstantAdapter
import io.plastique.core.json.OffsetCursorAdapter
import io.plastique.core.json.StringCursorAdapter
import io.plastique.core.json.ZonedDateTimeAdapter
import io.plastique.core.session.SessionManager
import io.plastique.notifications.MessageDtoSubjectJsonAdapterFactory
import org.threeten.bp.format.DateTimeFormatter

@Module
abstract class ApiModule {
    @Binds
    abstract fun bindErrorResponseParser(impl: ErrorResponseParserImpl): ErrorResponseParser

    @Module
    companion object {
        @Provides
        @Reusable
        @JvmStatic
        fun provideAuthService(apiClient: ApiClient): AuthService = apiClient.createService()

        @Provides
        @Reusable
        @JvmStatic
        fun provideCommentService(apiClient: ApiClient): CommentService = apiClient.createService()

        @Provides
        @Reusable
        @JvmStatic
        fun provideCollectionService(apiClient: ApiClient): CollectionService = apiClient.createService()

        @Provides
        @Reusable
        @JvmStatic
        fun provideDeviationService(apiClient: ApiClient): DeviationService = apiClient.createService()

        @Provides
        @Reusable
        @JvmStatic
        fun provideFeedService(apiClient: ApiClient): FeedService = apiClient.createService()

        @Provides
        @Reusable
        @JvmStatic
        fun provideGalleryService(apiClient: ApiClient): GalleryService = apiClient.createService()

        @Provides
        @Reusable
        @JvmStatic
        fun provideMessageService(apiClient: ApiClient): MessageService = apiClient.createService()

        @Provides
        @Reusable
        @JvmStatic
        fun provideStatusService(apiClient: ApiClient): StatusService = apiClient.createService()

        @Provides
        @Reusable
        @JvmStatic
        fun provideUserService(apiClient: ApiClient): UserService = apiClient.createService()

        @Provides
        @Reusable
        @JvmStatic
        fun provideWatchService(apiClient: ApiClient): WatchService = apiClient.createService()

        @Provides
        @Reusable
        @JvmStatic
        fun provideApiConfiguration(context: Context): ApiConfiguration = ApiConfiguration(
            apiUrl = ApiConstants.URL,
            clientId = context.getString(R.string.api_client_id),
            clientSecret = context.getString(R.string.api_client_secret),
            authRedirectUrl = "${context.packageName}://auth",
            apiVersion = ApiConstants.VERSION,
            userAgent = "Plastique/android ${BuildConfig.VERSION_NAME}")

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
                .add(MessageDtoSubjectJsonAdapterFactory())
                .build()
        }

        @Provides
        @JvmStatic
        fun provideAccessTokenProvider(sessionManager: Lazy<SessionManager>): AccessTokenProvider = object : AccessTokenProvider {
            override fun getAccessToken(invalidatedAccessToken: String?): String {
                return sessionManager.get().getAccessToken(invalidatedAccessToken)
            }
        }
    }
}
