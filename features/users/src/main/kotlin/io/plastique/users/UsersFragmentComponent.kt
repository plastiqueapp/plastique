package io.plastique.users

import io.plastique.users.profile.about.AboutFragment

interface UsersFragmentComponent {
    fun inject(fragment: AboutFragment)
}
