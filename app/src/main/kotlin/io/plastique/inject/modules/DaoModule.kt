package io.plastique.inject.modules

import dagger.Module
import dagger.Provides
import io.plastique.AppDatabase
import io.plastique.collections.CollectionDao
import io.plastique.comments.CommentDao
import io.plastique.core.cache.CacheEntryDao
import io.plastique.deviations.DeviationDao
import io.plastique.deviations.DeviationMetadataDao
import io.plastique.deviations.categories.CategoryDao
import io.plastique.deviations.download.DownloadInfoDao
import io.plastique.gallery.GalleryDao
import io.plastique.users.UserDao

@Module
object DaoModule {
    @Provides
    @JvmStatic
    fun provideCacheEntryDao(database: AppDatabase): CacheEntryDao = database.cacheEntryDao()

    @Provides
    @JvmStatic
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()

    @Provides
    @JvmStatic
    fun provideCollectionDao(database: AppDatabase): CollectionDao = database.collectionDao()

    @Provides
    @JvmStatic
    fun provideCommentDao(database: AppDatabase): CommentDao = database.commentDao()

    @Provides
    @JvmStatic
    fun provideDeviationDao(database: AppDatabase): DeviationDao = database.deviationDao()

    @Provides
    @JvmStatic
    fun provideDeviationMetadataDao(database: AppDatabase): DeviationMetadataDao = database.deviationMetadataDao()

    @Provides
    @JvmStatic
    fun provideDownloadInfoDao(database: AppDatabase): DownloadInfoDao = database.downloadInfoDao()

    @Provides
    @JvmStatic
    fun provideGalleryDao(database: AppDatabase): GalleryDao = database.galleryDao()

    @Provides
    @JvmStatic
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()
}
