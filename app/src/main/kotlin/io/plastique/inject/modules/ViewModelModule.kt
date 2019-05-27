package io.plastique.inject.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import io.plastique.auth.LoginViewModel
import io.plastique.collections.CollectionsViewModel
import io.plastique.comments.list.CommentListViewModel
import io.plastique.core.mvvm.DaggerViewModelFactory
import io.plastique.deviations.BrowseDeviationsViewModel
import io.plastique.deviations.categories.list.CategoryListViewModel
import io.plastique.deviations.info.DeviationInfoViewModel
import io.plastique.deviations.list.DeviationListViewModel
import io.plastique.deviations.viewer.DeviationViewerViewModel
import io.plastique.feed.FeedViewModel
import io.plastique.feed.settings.FeedSettingsViewModel
import io.plastique.gallery.GalleryViewModel
import io.plastique.main.MainViewModel
import io.plastique.notifications.NotificationsViewModel
import io.plastique.profile.ProfileViewModel
import io.plastique.settings.licenses.LicensesViewModel
import io.plastique.statuses.list.StatusListViewModel
import io.plastique.users.profile.UserProfileViewModel
import io.plastique.users.profile.about.AboutViewModel
import io.plastique.watch.WatcherListViewModel

@Module
interface ViewModelModule {
    @Binds
    fun bindViewModelFactory(impl: DaggerViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ClassKey(AboutViewModel::class)
    fun bindAboutViewModel(impl: AboutViewModel): ViewModel

    @Binds
    @IntoMap
    @ClassKey(BrowseDeviationsViewModel::class)
    fun bindBrowseDeviationsViewModel(impl: BrowseDeviationsViewModel): ViewModel

    @Binds
    @IntoMap
    @ClassKey(CategoryListViewModel::class)
    fun bindCategoryListViewModel(impl: CategoryListViewModel): ViewModel

    @Binds
    @IntoMap
    @ClassKey(CollectionsViewModel::class)
    fun bindCollectionsViewModel(impl: CollectionsViewModel): ViewModel

    @Binds
    @IntoMap
    @ClassKey(CommentListViewModel::class)
    fun bindCommentListViewModel(impl: CommentListViewModel): ViewModel

    @Binds
    @IntoMap
    @ClassKey(DeviationInfoViewModel::class)
    fun bindDeviationInfoViewModel(impl: DeviationInfoViewModel): ViewModel

    @Binds
    @IntoMap
    @ClassKey(DeviationListViewModel::class)
    fun bindDeviationListViewModel(impl: DeviationListViewModel): ViewModel

    @Binds
    @IntoMap
    @ClassKey(DeviationViewerViewModel::class)
    fun bindDeviationViewerViewModel(impl: DeviationViewerViewModel): ViewModel

    @Binds
    @IntoMap
    @ClassKey(FeedSettingsViewModel::class)
    fun bindFeedSettingsViewModel(impl: FeedSettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ClassKey(FeedViewModel::class)
    fun bindFeedViewModel(impl: FeedViewModel): ViewModel

    @Binds
    @IntoMap
    @ClassKey(GalleryViewModel::class)
    fun bindGalleryViewModel(impl: GalleryViewModel): ViewModel

    @Binds
    @IntoMap
    @ClassKey(LicensesViewModel::class)
    fun bindLicensesViewModel(impl: LicensesViewModel): ViewModel

    @Binds
    @IntoMap
    @ClassKey(LoginViewModel::class)
    fun bindLoginViewModel(impl: LoginViewModel): ViewModel

    @Binds
    @IntoMap
    @ClassKey(MainViewModel::class)
    fun bindMainViewModel(impl: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ClassKey(NotificationsViewModel::class)
    fun bindNotificationsViewModel(impl: NotificationsViewModel): ViewModel

    @Binds
    @IntoMap
    @ClassKey(ProfileViewModel::class)
    fun bindProfileViewModel(impl: ProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ClassKey(StatusListViewModel::class)
    fun bindStatusListViewModel(impl: StatusListViewModel): ViewModel

    @Binds
    @IntoMap
    @ClassKey(UserProfileViewModel::class)
    fun bindUserProfileViewModel(impl: UserProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ClassKey(WatcherListViewModel::class)
    fun bindWatcherListViewModel(impl: WatcherListViewModel): ViewModel
}
