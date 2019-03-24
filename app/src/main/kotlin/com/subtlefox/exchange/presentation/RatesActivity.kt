package com.subtlefox.exchange.presentation

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.subtlefox.exchange.R
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class RatesActivity : AppCompatActivity() {

    @Inject
    lateinit var listAdapter: RatesListAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val compositeDisposable = CompositeDisposable()
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as App).appComponent.plus().inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_exchange_rates)

        recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
            .apply {
                layoutManager = LinearLayoutManager(context)
                adapter = listAdapter
                setHasFixedSize(true)
                addOnScrollListener(scrollListener)
            }

        ViewModelProviders.of(this, viewModelFactory).get(RatesViewModel::class.java)
            .run {
                lifecycle.addObserver(this)
                observeData()
                observeNetworkStatus()
                observeSelection()
            }
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private fun RatesViewModel.observeData() {
        val loadingView = findViewById<TextView>(R.id.txt_loading)
        data.observe(this@RatesActivity, Observer { list ->
            loadingView.visibility = View.GONE
            listAdapter.data = list ?: emptyList()
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

    private fun RatesViewModel.observeSelection() {
        compositeDisposable.add(
            listAdapter
                .observeClicks()
                .mergeWith(listAdapter.observeCurrentValue())
                .distinctUntilChanged()
                .subscribe({ setBaseCurrency(it.second) }) { it.printStackTrace() }
        )
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        var state = RecyclerView.SCROLL_STATE_IDLE

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (state == RecyclerView.SCROLL_STATE_IDLE && newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                state = RecyclerView.SCROLL_STATE_DRAGGING

                currentFocus?.run {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(windowToken, 0)
                }
            } else if (newState != RecyclerView.SCROLL_STATE_DRAGGING) {
                state = RecyclerView.SCROLL_STATE_IDLE
            }
            super.onScrollStateChanged(recyclerView, newState)
        }
    }

}
