package io.plastique.inject.modules

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.Reusable
import io.plastique.api.auth.AuthService
import io.plastique.api.collections.CollectionService
import io.plastique.api.comments.CommentService
import io.plastique.api.deviations.DeviationService
import io.plastique.api.gallery.GalleryService
import io.plastique.api.users.UserService
import io.plastique.core.adapters.OffsetCursorAdapter
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
    fun provideGalleryService(apiClient: ApiClient): GalleryService {
        return apiClient.getService(GalleryService::class.java)
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
    fun provideMoshi(): Moshi {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
        return Moshi.Builder()
                .add(ZonedDateTimeAdapter(dateTimeFormatter))
                .add(OffsetCursorAdapter())
                .build()
    }
}
