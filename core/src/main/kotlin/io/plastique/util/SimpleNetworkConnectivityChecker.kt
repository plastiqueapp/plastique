package io.plastique.util

import android.content.Context
import android.net.ConnectivityManager
import javax.inject.Inject

class SimpleNetworkConnectivityChecker @Inject constructor(context: Context) : NetworkConnectivityChecker {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun isConnectedToNetwork(): Boolean {
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}
