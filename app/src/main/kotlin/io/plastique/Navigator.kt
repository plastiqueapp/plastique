package io.plastique

import io.plastique.auth.LoginActivity
import io.plastique.collections.CollectionFolderId
import io.plastique.collections.CollectionsActivity
import io.plastique.collections.CollectionsNavigator
import io.plastique.comments.CommentTarget
import io.plastique.comments.CommentsNavigator
import io.plastique.comments.list.CommentListActivity
import io.plastique.core.navigation.NavigationContext
import io.plastique.deviations.DeviationsNavigator
import io.plastique.deviations.viewer.DeviationViewerActivity
import io.plastique.gallery.GalleryActivity
import io.plastique.gallery.GalleryFolderId
import io.plastique.gallery.GalleryNavigator
import io.plastique.main.MainNavigator
import io.plastique.profile.ProfileNavigator
import io.plastique.settings.SettingsActivity
import io.plastique.settings.SettingsNavigator
import io.plastique.users.UserProfileActivity
import io.plastique.users.UsersNavigator
import io.plastique.util.Intents
import javax.inject.Inject
import javax.inject.Singleton
import io.plastique.collections.FolderDeviationListActivity as CollectionFolderDeviationListActivity
import io.plastique.gallery.FolderDeviationListActivity as GalleryFolderDeviationListActivity

@Singleton
class Navigator @Inject constructor() :
        CollectionsNavigator,
        CommentsNavigator,
        DeviationsNavigator,
        GalleryNavigator,
        MainNavigator,
        ProfileNavigator,
        SettingsNavigator,
        UsersNavigator {

    override fun openCollections(navigationContext: NavigationContext, username: String) {
        navigationContext.startActivity(CollectionsActivity.createIntent(navigationContext.context, username))
    }

    override fun openCollectionFolder(navigationContext: NavigationContext, folderId: CollectionFolderId, folderName: String) {
        navigationContext.startActivity(CollectionFolderDeviationListActivity.createIntent(navigationContext.context, folderId, folderName))
    }

    override fun openCommentsForDeviation(navigationContext: NavigationContext, deviationId: String) {
        navigationContext.startActivity(CommentListActivity.createIntent(navigationContext.context, CommentTarget.Deviation(deviationId)))
    }

    override fun openCommentsForUserProfile(navigationContext: NavigationContext, username: String) {
        navigationContext.startActivity(CommentListActivity.createIntent(navigationContext.context, CommentTarget.Profile(username)))
    }

    override fun openDeviation(navigationContext: NavigationContext, deviationId: String) {
        navigationContext.startActivity(DeviationViewerActivity.createIntent(navigationContext.context, deviationId))
    }

    override fun openGallery(navigationContext: NavigationContext, username: String) {
        navigationContext.startActivity(GalleryActivity.createIntent(navigationContext.context, username))
    }

    override fun openGalleryFolder(navigationContext: NavigationContext, folderId: GalleryFolderId, folderName: String) {
        navigationContext.startActivity(GalleryFolderDeviationListActivity.createIntent(navigationContext.context, folderId, folderName))
    }

    override fun openLogin(navigationContext: NavigationContext) {
        navigationContext.startActivity(LoginActivity.createIntent(navigationContext.context))
    }

    override fun openPlayStore(navigationContext: NavigationContext, packageName: String) {
        navigationContext.startActivity(Intents.openPlayStore(navigationContext.context, packageName))
    }

    override fun openSettings(navigationContext: NavigationContext) {
        navigationContext.startActivity(SettingsActivity.createIntent(navigationContext.context))
    }

    override fun openUserProfile(navigationContext: NavigationContext, username: String) {
        navigationContext.startActivity(UserProfileActivity.createIntent(navigationContext.context, username))
    }
}
