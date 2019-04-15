package com.subtlefox.currencyrates.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.support.v4.content.ContextCompat.getSystemService
import com.subtlefox.currencyrates.domain.repository.ConnectivityRepository
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class InternetConnectivity
@Inject constructor(
    ctx: Context
) : ConnectivityRepository {

    private val connectivityManager by lazy {
        getSystemService(ctx, ConnectivityManager::class.java)!!
    }

    private val requestBuilder by lazy {
        NetworkRequest.Builder()
            .addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private var networkObserver: AvailableNetworkObserver? = null

    override fun observeConnectivity(): Observable<Boolean> {
        return Observable
            .create<Boolean> { emitter ->
                networkObserver = AvailableNetworkObserver(connectivityManager, emitter)
                connectivityManager.registerNetworkCallback(requestBuilder.build(), networkObserver)
            }
            .startWith(isConnected().toObservable())
            .doOnDispose(::dispose)
    }

    override fun isConnected(): Single<Boolean> {
        return Single.create { emitter ->
            val isConnected = connectivityManager.activeNetworkInfo?.isConnected ?: false
            emitter.onSuccess(isConnected)
        }
    }

    private fun dispose() {
        connectivityManager.unregisterNetworkCallback(networkObserver)
    }

}