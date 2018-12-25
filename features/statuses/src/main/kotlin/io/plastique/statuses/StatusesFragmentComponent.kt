package io.plastique.statuses

import io.plastique.statuses.list.StatusListFragment

interface StatusesFragmentComponent {
    fun inject(fragment: StatusListFragment)
}
