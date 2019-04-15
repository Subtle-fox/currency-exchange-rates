package com.subtlefox.currencyrates.presentation

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.TextView
import com.subtlefox.currencyrates.App
import com.subtlefox.currencyrates.R
import com.subtlefox.currencyrates.presentation.utils.hideKeyboard
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class RatesActivity : AppCompatActivity() {

    @Inject
    lateinit var listAdapter: RatesListAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val compositeDisposable = CompositeDisposable()
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: RatesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as App).appComponent.plus().inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_exchange_rates)

        recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
            .apply {
                layoutManager = LinearLayoutManager(context)
                adapter = listAdapter
                itemAnimator?.moveDuration = 400
                setHasFixedSize(true)
                addOnScrollListener(scrollListener)
            }

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(RatesViewModel::class.java)
            .apply {
                lifecycle.addObserver(this)
                observeData()
                observeNetworkStatus()
            }
    }

    override fun onStart() {
        super.onStart()
        observeActiveCurrency()
    }

    override fun onStop() {
        compositeDisposable.clear()
        super.onStop()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private fun RatesViewModel.observeData() {
        val loadingView = findViewById<TextView>(R.id.txt_loading)
        currenciesData.observe(this@RatesActivity, Observer { list ->
            loadingView.visibility = View.GONE

            if (recyclerView.isAnimating || scrollListener.isScrollingOrFlinging()) {
                Log.d(TAG, "scrolling or animating => drop update (will update next second)")
            } else {
                listAdapter.data = list ?: emptyList()
            }
        })
    }

    private fun RatesViewModel.observeNetworkStatus() {
        val networkStatusView = findViewById<TextView>(R.id.network_status)
        networkStatus.observe(this@RatesActivity, Observer {
            if (it == true) {
                networkStatusView.setText(R.string.status_online)
                networkStatusView.setBackgroundResource(R.color.status_online)
            } else {
                networkStatusView.setText(R.string.status_offline)
                networkStatusView.setBackgroundResource(R.color.status_offline)
            }
        })
    }

    private fun observeActiveCurrency() {
        compositeDisposable.add(
            listAdapter
                .clicks()
                .mergeWith(listAdapter.textChanges())
                .subscribe(viewModel::setBaseCurrency) { Log.e(TAG, it.message, it) }
        )
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        private var state = RecyclerView.SCROLL_STATE_IDLE

        fun isScrollingOrFlinging() = state != RecyclerView.SCROLL_STATE_IDLE

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            state = newState
            if (isScrollingOrFlinging()) {
                currentFocus?.hideKeyboard()
            }
            super.onScrollStateChanged(recyclerView, newState)
        }
    }

    companion object {
        const val TAG = "Rates"
    }

}
