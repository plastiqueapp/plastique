package io.plastique.inject.modules

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import io.plastique.collections.folders.CollectionFolderRepository
import io.plastique.collections.folders.CollectionFolderRepositoryImpl
import io.plastique.comments.CommentRepository
import io.plastique.comments.CommentRepositoryImpl
import io.plastique.core.cache.CleanableRepository
import io.plastique.deviations.DeviationRepository
import io.plastique.deviations.DeviationRepositoryImpl
import io.plastique.feed.FeedRepository
import io.plastique.gallery.folders.GalleryFolderRepository
import io.plastique.notifications.MessageRepository
import io.plastique.statuses.StatusRepository
import io.plastique.statuses.StatusRepositoryImpl
import io.plastique.users.UserRepository
import io.plastique.users.UserRepositoryImpl

@Module
interface RepositoryModule {
    @Binds
    fun bindCollectionFolderRepository(impl: CollectionFolderRepositoryImpl): CollectionFolderRepository

    @Binds
    fun bindCommentRepository(impl: CommentRepositoryImpl): CommentRepository

    @Binds
    fun bindDeviationRepository(impl: DeviationRepositoryImpl): DeviationRepository

    @Binds
    fun bindStatusRepository(impl: StatusRepositoryImpl): StatusRepository

    @Binds
    fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @IntoSet
    fun bindCollectionFolderCleanableRepository(impl: CollectionFolderRepository): CleanableRepository

    @Binds
    @IntoSet
    fun bindFeedCleanableRepository(impl: FeedRepository): CleanableRepository

    @Binds
    @IntoSet
    fun bindGalleryFolderCleanableRepository(impl: GalleryFolderRepository): CleanableRepository

    @Binds
    @IntoSet
    fun bindMessageCleanableRepository(impl: MessageRepository): CleanableRepository
}
