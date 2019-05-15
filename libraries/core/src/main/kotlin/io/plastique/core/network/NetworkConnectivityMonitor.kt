package io.plastique.core.network

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

enum class NetworkConnectionState {
    Connected,
    Disconnected
}

interface NetworkConnectivityMonitor {
    val connectionState: Observable<NetworkConnectionState>
}

@Singleton
@SuppressLint("TimberTagLength")
class NetworkConnectivityMonitorImpl @Inject constructor(
    private val context: Context,
    private val connectivityChecker: NetworkConnectivityChecker
) : NetworkConnectivityMonitor {

    override val connectionState: Observable<NetworkConnectionState> by lazy {
        Observable.create<NetworkConnectionState> { emitter ->
            val receiver = ConnectionStateReceiver(emitter)
            emitter.setDisposable(receiver)
            emitter.onNext(currentConnectionState)
        }
            .subscribeOn(AndroidSchedulers.mainThread())
            .distinctUntilChanged()
            .doOnNext { Timber.tag(LOG_TAG).d("Connection state changed to %s", it) }
            .share()
    }

    private val currentConnectionState: NetworkConnectionState
        get() = if (connectivityChecker.isConnectedToNetwork) NetworkConnectionState.Connected else NetworkConnectionState.Disconnected

    private inner class ConnectionStateReceiver(
        private val emitter: ObservableEmitter<NetworkConnectionState>
    ) : BroadcastReceiver(), Disposable {
        @Volatile private var disposed = false

        init {
            @Suppress("DEPRECATION")
            context.registerReceiver(this, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        }

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
