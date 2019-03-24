package com.subtlefox.exchange.presentation

import android.arch.lifecycle.*
import android.util.Log
import com.subtlefox.exchange.domain.ConverterUseCase
import com.subtlefox.exchange.domain.CurrencyValue
import com.subtlefox.exchange.domain.GetDetailsUseCase
import com.subtlefox.exchange.domain.ObserveConnectivityUseCase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.math.BigDecimal
import javax.inject.Inject

class RatesViewModel
@Inject constructor(
    private val converterUseCase: ConverterUseCase,
    private val detailsUseCase: GetDetailsUseCase,
    private val networkConnectivity: ObserveConnectivityUseCase,
    private val mapper: ModelsMapper
) : ViewModel(), LifecycleObserver {

    private val compositeDisposable = CompositeDisposable()
    private val uiEventsSubject = BehaviorSubject.create<CurrencyValue>()
    private val _data = MutableLiveData<List<CurrencyUiModel>>()
    private val _networkStatus = MutableLiveData<Boolean>()

    val data: LiveData<List<CurrencyUiModel>> = _data
    val networkStatus: MutableLiveData<Boolean> = _networkStatus

    private var baseCurrencyIso = "EUR"

    init {
        uiEventsSubject.onNext(CurrencyValue(baseCurrencyIso, BigDecimal.ONE))
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

    fun setBaseCurrency(model: CurrencyUiModel) {
        val value = if (model.value.isEmpty()) {
            BigDecimal.ZERO
        } else {
            model.value.toBigDecimal()
        }
        baseCurrencyIso = model.iso
        uiEventsSubject.onNext(CurrencyValue(baseCurrencyIso, value))
    }

    private fun observeData() {
        compositeDisposable.add(
            Observable
                .combineLatest(
                    converterUseCase.stream(uiEventsSubject.observeOn(Schedulers.computation())),
                    detailsUseCase.load(),
                    mapper
                )
                .observeOn(Schedulers.computation())
                .subscribe(_data::postValue) { Log.e("VM", "values observer failed", it) }
        )
    }

    private fun observeConnectivity() {
        compositeDisposable.add(
            networkConnectivity
                .stream()
                .subscribe(_networkStatus::postValue) {
                    Log.e("VM", "connectivity observer failed", it)
                }
        )
    }
}