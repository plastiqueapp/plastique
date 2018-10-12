package io.plastique.util

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.disposables.Disposable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

enum class NetworkConnectionState {
    Connected,
    Disconnected
}

interface NetworkConnectivityMonitor {
    val isConnectedToNetwork: Boolean

    val connectionState: Observable<NetworkConnectionState>
}

@Singleton
@SuppressLint("TimberTagLength")
class NetworkConnectivityMonitorImpl @Inject constructor(private val context: Context) : NetworkConnectivityMonitor {
    private val connectivityManager = context.getSystemService<ConnectivityManager>()!!

    override val isConnectedToNetwork: Boolean
        get() = connectivityManager.activeNetworkInfo?.isConnected ?: false

    override val connectionState: Observable<NetworkConnectionState> by lazy {
        Observable.create<NetworkConnectionState> { emitter ->
            val receiver = ConnectionStateReceiver(emitter)
            emitter.setDisposable(receiver)
            emitter.onNext(currentConnectionState)
            context.registerReceiver(receiver, IntentFilter(
                    @Suppress("DEPRECATION") ConnectivityManager.CONNECTIVITY_ACTION))
        }
                .distinctUntilChanged()
                .doOnNext { Timber.tag(LOG_TAG).d("Connection state changed to %s", it) }
                .share()
    }

    private val currentConnectionState: NetworkConnectionState
        get() = if (isConnectedToNetwork) NetworkConnectionState.Connected else NetworkConnectionState.Disconnected

    private inner class ConnectionStateReceiver(
        private val emitter: ObservableEmitter<NetworkConnectionState>
    ) : BroadcastReceiver(), Disposable {
        @Volatile private var disposed = false

        override fun isDisposed(): Boolean = disposed

        override fun dispose() {
            disposed = true
            context.unregisterReceiver(this)
        }

        override fun onReceive(context: Context, intent: Intent) {
            emitter.onNext(currentConnectionState)
        }
    }

    private companion object {
        private const val LOG_TAG = "NetworkConnectivityMonitor"
    }
}