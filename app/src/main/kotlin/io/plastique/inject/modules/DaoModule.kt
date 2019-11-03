package io.plastique.inject.modules

import dagger.Module
import dagger.Provides
import io.plastique.collections.CollectionDao
import io.plastique.comments.CommentDao
import io.plastique.core.cache.CacheEntryDao
import io.plastique.db.AppDatabase
import io.plastique.deviations.DeviationDao
import io.plastique.deviations.categories.CategoryDao
import io.plastique.deviations.download.DownloadInfoDao
import io.plastique.deviations.info.DeviationMetadataDao
import io.plastique.feed.FeedDao
import io.plastique.gallery.GalleryDao
import io.plastique.notifications.MessageDao
import io.plastique.statuses.StatusDao
import io.plastique.users.UserDao
import io.plastique.watch.WatchDao

@Module
object DaoModule {
    @Provides
    fun provideCacheEntryDao(database: AppDatabase): CacheEntryDao = database.cacheEntryDao()

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideCollectionDao(database: AppDatabase): CollectionDao = database.collectionDao()

    @Provides
    fun provideCommentDao(database: AppDatabase): CommentDao = database.commentDao()

    @Provides
    fun provideDeviationDao(database: AppDatabase): DeviationDao = database.deviationDao()

    @Provides
    fun provideDeviationMetadataDao(database: AppDatabase): DeviationMetadataDao = database.deviationMetadataDao()

    @Provides
    fun provideDownloadInfoDao(database: AppDatabase): DownloadInfoDao = database.downloadInfoDao()

    @Provides
    fun provideFeedDao(database: AppDatabase): FeedDao = database.feedDao()

    @Provides
    fun provideGalleryDao(database: AppDatabase): GalleryDao = database.galleryDao()

    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao = database.messageDao()

    @Provides
    fun provideStatusDao(database: AppDatabase): StatusDao = database.statusDao()

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    fun provideWatchDao(database: AppDatabase): WatchDao = database.watchDao()
}
