package com.subtlefox.exchange.data

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import io.reactivex.ObservableEmitter

class AvailableNetworkObserver(
    private val connectivityManager: ConnectivityManager,
    private val emitter: ObservableEmitter<Boolean>
) : ConnectivityManager.NetworkCallback() {

    override fun onAvailable(network: Network?) {
        if (!emitter.isDisposed) {
            emitter.onNext(true)
        }
    }

    override fun onLost(network: Network?) {
        if (!emitter.isDisposed) {
            emitter.onNext(false)
        }
    }

    override fun onCapabilitiesChanged(network: Network?, networkCapabilities: NetworkCapabilities?) {
        if (!emitter.isDisposed) {
            emitter.onNext(connectivityManager.activeNetworkInfo?.isConnected ?: false)
        }
    }

    override fun onUnavailable() {
        if (!emitter.isDisposed) {
            emitter.onNext(false)
        }
    }

}