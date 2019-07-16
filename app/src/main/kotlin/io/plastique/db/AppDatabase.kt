package io.plastique.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.plastique.BuildConfig
import io.plastique.collections.CollectionDao
import io.plastique.comments.CommentDao
import io.plastique.comments.CommentEntity
import io.plastique.comments.CommentLinkage
import io.plastique.core.cache.CacheEntry
import io.plastique.core.cache.CacheEntryDao
import io.plastique.core.db.converters.DurationConverter
import io.plastique.core.db.converters.InstantConverter
import io.plastique.core.db.converters.SizeConverter
import io.plastique.core.db.converters.ZonedDateTimeConverter
import io.plastique.deviations.DailyDeviationEntity
import io.plastique.deviations.DeviationDao
import io.plastique.deviations.DeviationEntity
import io.plastique.deviations.DeviationImageEntity
import io.plastique.deviations.DeviationLinkage
import io.plastique.deviations.DeviationVideoEntity
import io.plastique.deviations.categories.CategoryDao
import io.plastique.deviations.categories.CategoryEntity
import io.plastique.deviations.download.DownloadInfoDao
import io.plastique.deviations.download.DownloadInfoEntity
import io.plastique.deviations.info.DeviationMetadataDao
import io.plastique.deviations.info.DeviationMetadataEntity
import io.plastique.feed.FeedDao
import io.plastique.feed.FeedDeviationEntityWithRelations
import io.plastique.feed.FeedElementDeviation
import io.plastique.feed.FeedElementEntity
import io.plastique.gallery.GalleryDao
import io.plastique.notifications.DeletedMessageEntity
import io.plastique.notifications.MessageDao
import io.plastique.notifications.MessageEntity
import io.plastique.statuses.StatusDao
import io.plastique.statuses.StatusEntity
import io.plastique.statuses.StatusLinkage
import io.plastique.users.UserDao
import io.plastique.users.UserEntity
import io.plastique.users.profile.UserProfileEntity
import io.plastique.watch.WatchDao
import io.plastique.watch.WatcherEntity
import io.plastique.collections.DeletedFolderEntity as DeletedCollectionFolderEntity
import io.plastique.collections.FolderEntity as CollectionFolderEntity
import io.plastique.collections.FolderLinkage as CollectionFolderLinkage
import io.plastique.gallery.DeletedFolderEntity as DeletedGalleryFolderEntity
import io.plastique.gallery.FolderEntity as GalleryFolderEntity
import io.plastique.gallery.FolderLinkage as GalleryFolderLinkage

@Database(
    entities = [
        CacheEntry::class,
        CategoryEntity::class,

        CollectionFolderEntity::class,
        CollectionFolderLinkage::class,
        DeletedCollectionFolderEntity::class,

        CommentEntity::class,
        CommentLinkage::class,

        DeviationEntity::class,
        DailyDeviationEntity::class,
        DeviationImageEntity::class,
        DeviationVideoEntity::class,
        DeviationLinkage::class,
        DeviationMetadataEntity::class,
        DownloadInfoEntity::class,

        FeedElementEntity::class,
        FeedElementDeviation::class,

        GalleryFolderEntity::class,
        GalleryFolderLinkage::class,
        DeletedGalleryFolderEntity::class,

        StatusEntity::class,
        StatusLinkage::class,

        UserEntity::class,
        UserProfileEntity::class,
        WatcherEntity::class,
        MessageEntity::class,
        DeletedMessageEntity::class
    ],
    views = [
        FeedDeviationEntityWithRelations::class
    ],
    version = BuildConfig.DB_VERSION,
    exportSchema = false)
@TypeConverters(
    DurationConverter::class,
    InstantConverter::class,
    SizeConverter::class,
    ZonedDateTimeConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheEntryDao(): CacheEntryDao

    abstract fun categoryDao(): CategoryDao

    abstract fun collectionDao(): CollectionDao

    abstract fun commentDao(): CommentDao

    abstract fun deviationDao(): DeviationDao

    abstract fun deviationMetadataDao(): DeviationMetadataDao

    abstract fun downloadInfoDao(): DownloadInfoDao

    abstract fun feedDao(): FeedDao

    abstract fun galleryDao(): GalleryDao

    abstract fun messageDao(): MessageDao

    abstract fun statusDao(): StatusDao

    abstract fun userDao(): UserDao

    abstract fun watchDao(): WatchDao
}
