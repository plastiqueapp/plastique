package io.plastique.inject.components

import dagger.Subcomponent
import io.plastique.collections.CollectionsFragmentComponent
import io.plastique.comments.CommentsFragmentComponent
import io.plastique.deviations.DeviationsFragmentComponent
import io.plastique.feed.FeedFragmentComponent
import io.plastique.gallery.GalleryFragmentComponent
import io.plastique.inject.BaseFragmentComponent
import io.plastique.notifications.NotificationsFragmentComponent
import io.plastique.profile.ProfileFragmentComponent
import io.plastique.settings.SettingsFragmentComponent
import io.plastique.statuses.StatusesFragmentComponent
import io.plastique.users.UsersFragmentComponent

@Subcomponent
interface FragmentComponent :
    BaseFragmentComponent,
    CollectionsFragmentComponent,
    CommentsFragmentComponent,
    DeviationsFragmentComponent,
    FeedFragmentComponent,
    GalleryFragmentComponent,
    NotificationsFragmentComponent,
    ProfileFragmentComponent,
    SettingsFragmentComponent,
    StatusesFragmentComponent,
    UsersFragmentComponent
