package com.subtlefox.exchange.presentation

import android.support.v7.recyclerview.extensions.AsyncListDiffer
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.subtlefox.exchange.R
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject


class RatesListAdapter
@Inject constructor() : RecyclerView.Adapter<CurrencyViewHolder>() {

    private val onClickSubject = PublishSubject.create<Int>()
    private val onTextChangedSubject = PublishSubject.create<Pair<Int, CharSequence>>()
    private val differ = AsyncListDiffer(this, DiffUtilsCallback())

    fun observeClicks(): Observable<Pair<Int, CurrencyUiModel>> {
        return onClickSubject.map { Pair(it, data[it]) }
    }

    fun observeCurrentValue(): Observable<Pair<Int, CurrencyUiModel>> {
        return onTextChangedSubject
            .filter { data[it.first].isBase }
            .map { Pair(it.first, data[it.first].copy(value = it.second.toString())) }
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

    override fun onBindViewHolder(
        vh: CurrencyViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            val payload = payloads.last() as Pair<String, Boolean>
            if (payload.first != data[position].value) {
                vh.setValue(data[position])
            }

            if (payload.second != data[position].isBase) {
                vh.setSelected(data[position].isBase)
            }
        } else {
            super.onBindViewHolder(vh, position, payloads)
        }
    }

    override fun onFailedToRecycleView(holder: CurrencyViewHolder): Boolean {
        return false
    }

}