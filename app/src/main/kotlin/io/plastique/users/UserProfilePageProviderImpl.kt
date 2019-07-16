package io.plastique.users

import io.plastique.R
import io.plastique.collections.CollectionsFragment
import io.plastique.comments.CommentThreadId
import io.plastique.comments.list.CommentListFragment
import io.plastique.core.pager.FragmentListPagerAdapter.Page
import io.plastique.gallery.GalleryFragment
import io.plastique.statuses.list.StatusListFragment
import io.plastique.users.profile.UserProfilePageProvider
import io.plastique.users.profile.about.AboutFragment
import javax.inject.Inject

class UserProfilePageProviderImpl @Inject constructor() : UserProfilePageProvider {
    override fun getPages(username: String): List<Page> = listOf(
        Page(R.string.users_profile_tab_about, AboutFragment::class.java, AboutFragment.newArgs(username)),
        Page(R.string.users_profile_tab_gallery, GalleryFragment::class.java, GalleryFragment.newArgs(username)),
        Page(R.string.users_profile_tab_favorites, CollectionsFragment::class.java, CollectionsFragment.newArgs(username)),
        Page(R.string.users_profile_tab_activity, StatusListFragment::class.java, StatusListFragment.newArgs(username)),
        Page(R.string.users_profile_tab_comments, CommentListFragment::class.java, CommentListFragment.newArgs(CommentThreadId.Profile(username))))
}
