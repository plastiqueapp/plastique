package io.plastique.users.profile

import io.plastique.core.FragmentListPagerAdapter.Page

interface UserProfilePageProvider {
    fun getPages(username: String): List<Page>
}
