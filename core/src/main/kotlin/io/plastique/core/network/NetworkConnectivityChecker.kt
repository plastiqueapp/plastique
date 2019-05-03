package io.plastique.core.network

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import javax.inject.Inject
import javax.inject.Singleton

interface NetworkConnectivityChecker {
    val isConnectedToNetwork: Boolean
}

@Singleton
class NetworkConnectivityCheckerImpl @Inject constructor(context: Context) : NetworkConnectivityChecker {
    private val connectivityManager = context.getSystemService<ConnectivityManager>()!!

    override val isConnectedToNetwork: Boolean
        get() = connectivityManager.activeNetworkInfo?.isConnected ?: false
}
