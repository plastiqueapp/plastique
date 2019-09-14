package io.plastique.core.network

import android.content.Context
import android.net.ConnectivityManager
import com.github.technoir42.android.extensions.requireSystemService
import javax.inject.Inject
import javax.inject.Singleton

interface NetworkConnectivityChecker {
    val isConnectedToNetwork: Boolean
}

@Singleton
class NetworkConnectivityCheckerImpl @Inject constructor(context: Context) : NetworkConnectivityChecker {
    private val connectivityManager = context.requireSystemService<ConnectivityManager>()

    @Suppress("DEPRECATION")
    override val isConnectedToNetwork: Boolean
        get() = connectivityManager.activeNetworkInfo?.isConnected ?: false
}
