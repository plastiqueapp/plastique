package io.plastique.core.session

interface OnLogoutListener {
    fun onLogout()
}

inline fun onLogoutListener(crossinline action: () -> Unit): OnLogoutListener =
    object : OnLogoutListener {
        override fun onLogout() {
            action()
        }
    }
