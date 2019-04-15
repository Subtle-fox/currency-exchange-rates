package com.subtlefox.currencyrates.presentation

import android.support.v7.recyclerview.extensions.AsyncListDiffer
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.subtlefox.currencyrates.R
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class RatesListAdapter
@Inject constructor() : RecyclerView.Adapter<CurrencyViewHolder>() {

    private val onClickSubject = PublishSubject.create<Int>()
    private val onTextChangedSubject = PublishSubject.create<Pair<Int, CharSequence>>()
    private val differ = AsyncListDiffer(this, DiffUtilsCallback())

    fun clicks(): Observable<CurrencyUiModel> {
        return onClickSubject.map { data[it] }
    }

    fun textChanges(): Observable<CurrencyUiModel> {
        return onTextChangedSubject
            .throttleLast(50, TimeUnit.MILLISECONDS)
            .filter { data[it.first].isBase }
            .map { data[it.first].copy(value = it.second.toString()) }
    }

    var data: List<CurrencyUiModel> = emptyList()
        set(value) {
            field = value
            differ.submitList(field)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
        val itemView = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_exchange_rate, parent, false)
        return CurrencyViewHolder(itemView, onClickSubject, onTextChangedSubject)
    }

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(viewHolder: CurrencyViewHolder, position: Int) {
        viewHolder.bind(data[position])
    }

    override fun onBindViewHolder(vh: CurrencyViewHolder, pos: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            val (oldValue, oldIsBase) = payloads.last() as Pair<String, Boolean>
            if (!data[pos].isBase && oldValue != data[pos].value) {
                vh.setValue(data[pos].value)
            }

            if (oldIsBase != data[pos].isBase) {
                vh.setSelected(data[pos].isBase)
            }
        } else {
            super.onBindViewHolder(vh, pos, payloads)
        }
    }

    override fun onFailedToRecycleView(holder: CurrencyViewHolder): Boolean {
        return false
    }

}