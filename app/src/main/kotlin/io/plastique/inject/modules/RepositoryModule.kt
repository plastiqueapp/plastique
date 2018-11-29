package io.plastique.inject.modules

import dagger.Binds
import dagger.Module
import io.plastique.collections.CollectionFolderRepository
import io.plastique.collections.CollectionFolderRepositoryImpl
import io.plastique.deviations.DeviationRepository
import io.plastique.deviations.DeviationRepositoryImpl
import io.plastique.users.UserRepository
import io.plastique.users.UserRepositoryImpl

@Module
interface RepositoryModule {
    @Binds
    fun bindCollectionFolderRepository(impl: CollectionFolderRepositoryImpl): CollectionFolderRepository

    @Binds
    fun bindDeviationRepository(impl: DeviationRepositoryImpl): DeviationRepository

    @Binds
    fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
