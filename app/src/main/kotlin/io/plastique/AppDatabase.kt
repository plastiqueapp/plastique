package io.plastique

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.plastique.collections.CollectionDao
import io.plastique.comments.CommentDao
import io.plastique.comments.CommentEntity
import io.plastique.comments.CommentLinkage
import io.plastique.core.cache.CacheEntry
import io.plastique.core.cache.CacheEntryDao
import io.plastique.core.converters.InstantConverter
import io.plastique.core.converters.SizeConverter
import io.plastique.core.converters.ZonedDateTimeConverter
import io.plastique.deviations.DeviationDao
import io.plastique.deviations.DeviationEntity
import io.plastique.deviations.DeviationLinkage
import io.plastique.deviations.DeviationMetadataDao
import io.plastique.deviations.DeviationMetadataEntity
import io.plastique.deviations.categories.CategoryDao
import io.plastique.deviations.categories.CategoryEntity
import io.plastique.deviations.download.DownloadInfoDao
import io.plastique.deviations.download.DownloadInfoEntity
import io.plastique.gallery.GalleryDao
import io.plastique.users.UserDao
import io.plastique.users.UserEntity
import io.plastique.users.UserProfileEntity
import io.plastique.watch.WatchDao
import io.plastique.watch.WatcherEntity
import io.plastique.collections.FolderEntity as CollectionFolderEntity
import io.plastique.collections.FolderLinkage as CollectionFolderLinkage
import io.plastique.gallery.FolderEntity as GalleryFolderEntity
import io.plastique.gallery.FolderLinkage as GalleryFolderLinkage

@Database(entities = [
    CacheEntry::class,
    CategoryEntity::class,

    CollectionFolderEntity::class,
    CollectionFolderLinkage::class,

    CommentEntity::class,
    CommentLinkage::class,

    DeviationEntity::class,
    DeviationLinkage::class,
    DeviationMetadataEntity::class,
    DownloadInfoEntity::class,

    GalleryFolderEntity::class,
    GalleryFolderLinkage::class,

    UserEntity::class,
    UserProfileEntity::class,
    WatcherEntity::class
], version = BuildConfig.DB_VERSION, exportSchema = false)
@TypeConverters(InstantConverter::class, SizeConverter::class, ZonedDateTimeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheEntryDao(): CacheEntryDao

    abstract fun categoryDao(): CategoryDao

    abstract fun collectionDao(): CollectionDao

    abstract fun commentDao(): CommentDao

    abstract fun deviationDao(): DeviationDao

    abstract fun deviationMetadataDao(): DeviationMetadataDao

    abstract fun downloadInfoDao(): DownloadInfoDao

    abstract fun galleryDao(): GalleryDao

    abstract fun userDao(): UserDao

    abstract fun watchDao(): WatchDao
}
