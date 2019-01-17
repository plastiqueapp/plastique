package io.plastique

import io.plastique.auth.LoginActivity
import io.plastique.collections.CollectionFolderId
import io.plastique.collections.CollectionsNavigator
import io.plastique.comments.CommentThreadId
import io.plastique.comments.CommentsNavigator
import io.plastique.comments.list.CommentListActivity
import io.plastique.core.BrowserLauncher
import io.plastique.core.navigation.NavigationContext
import io.plastique.deviations.DeviationsNavigator
import io.plastique.deviations.info.DeviationInfoActivity
import io.plastique.deviations.viewer.DeviationViewerActivity
import io.plastique.feed.FeedNavigator
import io.plastique.gallery.GalleryFolderId
import io.plastique.gallery.GalleryNavigator
import io.plastique.main.MainNavigator
import io.plastique.profile.ProfileNavigator
import io.plastique.settings.SettingsActivity
import io.plastique.settings.SettingsNavigator
import io.plastique.statuses.ShareObjectId
import io.plastique.statuses.StatusesNavigator
import io.plastique.users.User
import io.plastique.users.UserProfileActivity
import io.plastique.users.UserType
import io.plastique.users.UsersNavigator
import io.plastique.util.Intents
import io.plastique.watch.WatchNavigator
import io.plastique.watch.WatcherListActivity
import javax.inject.Inject
import javax.inject.Singleton
import io.plastique.collections.FolderDeviationListActivity as CollectionFolderDeviationListActivity
import io.plastique.gallery.FolderDeviationListActivity as GalleryFolderDeviationListActivity

@Singleton
class Navigator @Inject constructor(private val browserLauncher: BrowserLauncher) :
        CollectionsNavigator,
        CommentsNavigator,
        DeviationsNavigator,
        FeedNavigator,
        GalleryNavigator,
        MainNavigator,
        ProfileNavigator,
        SettingsNavigator,
        StatusesNavigator,
        UsersNavigator,
        WatchNavigator {

    override fun openCollectionFolder(navigationContext: NavigationContext, username: String?, folderId: String, folderName: String) {
        navigationContext.startActivity(CollectionFolderDeviationListActivity.createIntent(navigationContext.context, CollectionFolderId(folderId, username), folderName))
    }

    override fun openComments(navigationContext: NavigationContext, threadId: CommentThreadId) {
        navigationContext.startActivity(CommentListActivity.createIntent(navigationContext.context, threadId))
    }

    override fun openDeviation(navigationContext: NavigationContext, deviationId: String) {
        navigationContext.startActivity(DeviationViewerActivity.createIntent(navigationContext.context, deviationId))
    }

    override fun openDeviationInfo(navigationContext: NavigationContext, deviationId: String) {
        navigationContext.startActivity(DeviationInfoActivity.createIntent(navigationContext.context, deviationId))
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

    override fun openStatus(navigationContext: NavigationContext, statusId: String) {
        // TODO: Open status with comments
        openComments(navigationContext, CommentThreadId.Status(statusId))
    }

    override fun openPostStatus(navigationContext: NavigationContext, shareObjectId: ShareObjectId?) {
        // TODO
    }

    override fun openTag(navigationContext: NavigationContext, tag: String) {
        // TODO
    }

    override fun openUserProfile(navigationContext: NavigationContext, user: User) {
        if (user.type == UserType.Group) {
            openUrl(navigationContext, "https://www.deviantart.com/${user.name}")
        } else {
            navigationContext.startActivity(UserProfileActivity.createIntent(navigationContext.context, user.name))
        }
    }

    override fun openWatchers(navigationContext: NavigationContext, username: String?) {
        navigationContext.startActivity(WatcherListActivity.createIntent(navigationContext.context, username))
    }

    override fun openUrl(navigationContext: NavigationContext, url: String) {
        browserLauncher.openUrl(navigationContext.context, url)
    }
}
