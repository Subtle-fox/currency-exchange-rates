package com.subtlefox.exchange.presentation

import android.support.v7.util.DiffUtil

class DiffUtilsCallback
    : DiffUtil.ItemCallback<CurrencyUiModel>() {

    override fun areItemsTheSame(oldItem: CurrencyUiModel, newItem: CurrencyUiModel): Boolean {
        return oldItem.iso.hashCode() == newItem.iso.hashCode()
    }

    override fun areContentsTheSame(oldItem: CurrencyUiModel, newItem: CurrencyUiModel): Boolean {
        return oldItem.iso == newItem.iso
                && oldItem.value == newItem.value
                && oldItem.isBase == newItem.isBase
    }

    override fun getChangePayload(oldItem: CurrencyUiModel, newItem: CurrencyUiModel): Pair<String, Boolean> {
        return Pair(oldItem.value, oldItem.isBase)
    }
}
