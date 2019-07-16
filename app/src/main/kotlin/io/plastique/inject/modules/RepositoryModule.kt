package io.plastique.inject.modules

import dagger.Binds
import dagger.Module
import io.plastique.collections.folders.CollectionFolderRepository
import io.plastique.collections.folders.CollectionFolderRepositoryImpl
import io.plastique.comments.CommentRepository
import io.plastique.comments.CommentRepositoryImpl
import io.plastique.deviations.DeviationRepository
import io.plastique.deviations.DeviationRepositoryImpl
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
}
