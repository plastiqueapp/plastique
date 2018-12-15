package io.plastique.inject.components

import dagger.Subcomponent
import io.plastique.collections.CollectionsFragmentComponent
import io.plastique.comments.CommentsFragmentComponent
import io.plastique.deviations.DeviationsFragmentComponent
import io.plastique.feed.FeedFragmentComponent
import io.plastique.gallery.GalleryFragmentComponent
import io.plastique.inject.FragmentComponent
import io.plastique.inject.scopes.FragmentScope
import io.plastique.profile.ProfileFragmentComponent
import io.plastique.settings.SettingsFragmentComponent

@FragmentScope
@Subcomponent
interface ModuleFragmentComponent :
        FragmentComponent,
        CollectionsFragmentComponent,
        CommentsFragmentComponent,
        DeviationsFragmentComponent,
        FeedFragmentComponent,
        GalleryFragmentComponent,
        ProfileFragmentComponent,
        SettingsFragmentComponent
