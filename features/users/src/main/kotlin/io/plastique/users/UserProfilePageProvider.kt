package io.plastique.users

import io.plastique.core.FragmentListPagerAdapter.Page

interface UserProfilePageProvider {
    fun getPages(username: String): List<Page>
}
