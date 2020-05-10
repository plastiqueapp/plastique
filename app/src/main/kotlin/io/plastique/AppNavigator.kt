package io.plastique

import android.content.Context
import io.plastique.auth.AuthNavigator
import io.plastique.auth.LoginActivity
import io.plastique.collections.CollectionsNavigator
import io.plastique.collections.folders.CollectionFolderId
import io.plastique.comments.CommentThreadId
import io.plastique.comments.CommentsNavigator
import io.plastique.comments.list.CommentListActivity
import io.plastique.core.navigation.BaseNavigator
import io.plastique.core.navigation.Route
import io.plastique.deviations.DeviationsNavigator
import io.plastique.deviations.categories.Category
import io.plastique.deviations.categories.list.CategoryListActivity
import io.plastique.deviations.info.DeviationInfoActivity
import io.plastique.deviations.list.TimeRangeDialogFragment
import io.plastique.deviations.viewer.DeviationViewerActivity
import io.plastique.feed.FeedNavigator
import io.plastique.feed.settings.FeedSettingsFragment
import io.plastique.gallery.GalleryNavigator
import io.plastique.gallery.folders.GalleryFolderId
import io.plastique.main.MainNavigator
import io.plastique.notifications.NotificationsNavigator
import io.plastique.profile.ProfileNavigator
import io.plastique.settings.SettingsActivity
import io.plastique.settings.SettingsNavigator
import io.plastique.settings.licenses.LicensesActivity
import io.plastique.statuses.ShareObjectId
import io.plastique.statuses.StatusesNavigator
import io.plastique.users.User
import io.plastique.users.UserType
import io.plastique.users.UsersNavigator
import io.plastique.users.profile.UserProfileActivity
import io.plastique.util.Intents
import io.plastique.watch.WatchNavigator
import io.plastique.watch.WatcherListActivity
import javax.inject.Inject
import io.plastique.collections.deviations.FolderDeviationListActivity as CollectionFolderDeviationListActivity
import io.plastique.gallery.deviations.FolderDeviationListActivity as GalleryFolderDeviationListActivity

class AppNavigator @Inject constructor(private val context: Context) : BaseNavigator(),
    AuthNavigator,
    CollectionsNavigator,
    CommentsNavigator,
    DeviationsNavigator,
    FeedNavigator,
    GalleryNavigator,
    MainNavigator,
    NotificationsNavigator,
    ProfileNavigator,
    SettingsNavigator,
    StatusesNavigator,
    UsersNavigator,
    WatchNavigator {

    override fun openCategoryList(selectedCategory: Category, requestCode: Int) {
        navigateTo(CategoryListActivity.route(context, requestCode, selectedCategory))
    }

    override fun openCollectionFolder(folderId: CollectionFolderId, folderName: String) {
        navigateTo(CollectionFolderDeviationListActivity.route(context, folderId, folderName))
    }

    override fun openComments(threadId: CommentThreadId) {
        navigateTo(CommentListActivity.route(context, threadId))
    }

    override fun openDeviation(deviationId: String) {
        navigateTo(DeviationViewerActivity.route(context, deviationId))
    }

    override fun openDeviationInfo(deviationId: String) {
        navigateTo(DeviationInfoActivity.route(context, deviationId))
    }

    override fun openGalleryFolder(folderId: GalleryFolderId, folderName: String) {
        navigateTo(GalleryFolderDeviationListActivity.route(context, folderId, folderName))
    }

    override fun openLicenses() {
        navigateTo(LicensesActivity.route(context))
    }

    override fun openPlayStore(packageName: String) {
        navigateTo(Route.Activity(Intents.openPlayStore(context, packageName)))
    }

    override fun openSignIn() {
        navigateTo(LoginActivity.route(context))
    }

    override fun openSettings() {
        navigateTo(SettingsActivity.route(context))
    }

    override fun openStatus(statusId: String) {
        // TODO: Open status with comments
        openComments(CommentThreadId.Status(statusId))
    }

    override fun openPostStatus(shareObjectId: ShareObjectId?) {
        // TODO
    }

    override fun openTag(tag: String) {
        // TODO
    }

    override fun openUserProfile(user: User) {
        if (user.type == UserType.Group) {
            openUrl("https://www.deviantart.com/${user.name}")
        } else {
            navigateTo(UserProfileActivity.route(context, user.name))
        }
    }

    override fun openWatchers(username: String?) {
        navigateTo(WatcherListActivity.route(context, username))
    }

    override fun openUrl(url: String) {
        navigateTo(Route.Url(url))
    }

    override fun showFeedSettingsDialog(tag: String) {
        navigateTo(FeedSettingsFragment.route(tag))
    }

    override fun showTimeRangeDialog(tag: String) {
        navigateTo(TimeRangeDialogFragment.route(tag))
    }
}
