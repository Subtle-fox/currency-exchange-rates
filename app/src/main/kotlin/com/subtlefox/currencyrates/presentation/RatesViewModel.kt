package com.subtlefox.currencyrates.presentation

import androidx.lifecycle.*
import android.util.Log
import com.subtlefox.currencyrates.BuildConfig
import com.subtlefox.currencyrates.domain.ConvertRatesUseCase
import com.subtlefox.currencyrates.domain.CurrencyValue
import com.subtlefox.currencyrates.domain.GetDetailsUseCase
import com.subtlefox.currencyrates.domain.ObserveConnectivityUseCase
import com.subtlefox.currencyrates.presentation.utils.toBigDecimalOrZero
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import java.math.BigDecimal
import javax.inject.Inject

class RatesViewModel
@Inject constructor(
    private val convertRatesUseCase: ConvertRatesUseCase,
    private val detailsUseCase: GetDetailsUseCase,
    private val networkConnectivity: ObserveConnectivityUseCase,
    private val mapper: ModelsMapper
) : ViewModel(), LifecycleObserver {

    private val compositeDisposable = CompositeDisposable()
    private val baseCurrencySubject = BehaviorSubject.create<CurrencyValue>()
    private val _currencies = MutableLiveData<List<CurrencyUiModel>>()
    private val _network = MutableLiveData<Boolean>()
    private var baseIso = BuildConfig.START_ISO

    val currenciesData: LiveData<List<CurrencyUiModel>> = _currencies
    val networkStatus: MutableLiveData<Boolean> = _network

    init {
        baseCurrencySubject.onNext(CurrencyValue(baseIso, BigDecimal.ONE))
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun resume() {
        observeData()
        observeConnectivity()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun pause() {
        compositeDisposable.clear()
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    fun setBaseCurrency(request: CurrencyUiModel) {
        baseIso = request.iso
        val currencyValue = CurrencyValue(baseIso, request.value.toBigDecimalOrZero())
        baseCurrencySubject.onNext(currencyValue)
    }

    private fun observeData() {
        compositeDisposable.add(
            baseCurrencySubject
                .compose(convertRatesUseCase)
                .withLatestFrom(
                    detailsUseCase.load(),
                    mapper
                )
                .subscribe(_currencies::postValue) { Log.e(TAG, "values observer failed", it) }
        )
    }

    private fun observeConnectivity() {
        compositeDisposable.add(
            networkConnectivity
                .stream()
                .subscribe(_network::postValue) { Log.e(TAG, "network observer failed", it) }
        )
    }

    companion object {
        const val TAG = "RatesVm"
    }
}