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
import io.plastique.api.common.ApiConstants
import io.plastique.api.feed.FeedElementDto
import io.plastique.api.feed.FeedElementTypes
import io.plastique.api.statuses.StatusDto
import io.plastique.core.client.AccessTokenProvider
import io.plastique.core.client.ApiConfiguration
import io.plastique.core.client.ErrorResponseParser
import io.plastique.core.json.adapters.InstantAdapter
import io.plastique.core.json.adapters.OffsetCursorAdapter
import io.plastique.core.json.adapters.StringCursorAdapter
import io.plastique.core.json.adapters.ZonedDateTimeAdapter
import io.plastique.core.session.SessionManager
import io.plastique.notifications.MessageDtoSubjectJsonAdapterFactory
import org.threeten.bp.format.DateTimeFormatter

@Module(includes = [ApiServiceModule::class])
abstract class ApiModule {
    @Binds
    abstract fun bindErrorResponseParser(impl: ErrorResponseParserImpl): ErrorResponseParser

    companion object {
        @Provides
        @Reusable
        fun provideApiConfiguration(context: Context): ApiConfiguration = ApiConfiguration(
            apiUrl = ApiConstants.URL,
            clientId = context.getString(R.string.api_client_id),
            clientSecret = context.getString(R.string.api_client_secret),
            authRedirectUrl = "${context.packageName}://auth",
            apiVersion = ApiConstants.VERSION,
            userAgent = "Plastique/android ${BuildConfig.VERSION_NAME}",
            debug = BuildConfig.DEBUG)

        @Provides
        @Reusable
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
        fun provideAccessTokenProvider(sessionManager: Lazy<SessionManager>): AccessTokenProvider = object : AccessTokenProvider {
            override fun getAccessToken(invalidatedAccessToken: String?): String =
                sessionManager.get().getAccessToken(invalidatedAccessToken)
        }
    }
}
