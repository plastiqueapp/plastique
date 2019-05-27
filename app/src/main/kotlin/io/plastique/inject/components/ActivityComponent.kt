package io.plastique.inject.components

import dagger.Subcomponent
import io.plastique.auth.AuthActivityComponent
import io.plastique.collections.CollectionsActivityComponent
import io.plastique.comments.CommentsActivityComponent
import io.plastique.deviations.DeviationsActivityComponent
import io.plastique.gallery.GalleryActivityComponent
import io.plastique.inject.BaseActivityComponent
import io.plastique.inject.modules.NavigationModule
import io.plastique.main.MainActivityComponent
import io.plastique.settings.SettingsActivityComponent
import io.plastique.users.UsersActivityComponent
import io.plastique.watch.WatchActivityComponent

@Subcomponent(modules = [NavigationModule::class])
interface ActivityComponent :
    BaseActivityComponent,
    AuthActivityComponent,
    CollectionsActivityComponent,
    CommentsActivityComponent,
    DeviationsActivityComponent,
    GalleryActivityComponent,
    MainActivityComponent,
    SettingsActivityComponent,
    UsersActivityComponent,
    WatchActivityComponent {

    override fun createFragmentComponent(): FragmentComponent
}
