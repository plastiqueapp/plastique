package io.plastique.users.profile

import io.plastique.core.pager.FragmentListPagerAdapter.Page

interface UserProfilePageProvider {
    fun getPages(username: String): List<Page>
}
