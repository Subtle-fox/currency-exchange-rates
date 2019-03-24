package com.subtlefox.exchange.presentation

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import com.subtlefox.exchange.R
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject


@SuppressLint("ClickableViewAccessibility")
class CurrencyViewHolder(
    itemView: View,
    onClickObserver: Observer<in Int>,
    textChangesObservable: PublishSubject<Pair<Int, CharSequence>>
) : RecyclerView.ViewHolder(itemView) {

    private val currencyImg: ImageView = itemView.findViewById(R.id.icon_currency)
    private val txtCurrencyIso: TextView = itemView.findViewById(R.id.txt_currency_iso)
    private val txtCurrencyName: TextView = itemView.findViewById(R.id.txt_currency_name)
    private val editCurrencyValue: EditText = itemView.findViewById(R.id.edit_currency_value)
    private val textWatcher = ValueTextWatcher(textChangesObservable)

    init {
        editCurrencyValue.setOnTouchListener { _, event ->
            if (MotionEvent.ACTION_UP == event.action) {
                onClickObserver.onNext(adapterPosition)
                editCurrencyValue.addTextChangedListener(textWatcher)
            }
            false
        }

        editCurrencyValue.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                editCurrencyValue.removeTextChangedListener(textWatcher)
            }
        }
    }

    fun bind(model: CurrencyUiModel) {
        if (model.iconUrl.isEmpty()) {
            Picasso.get().load(R.drawable.ic_unknown_currency)
        } else {
            Picasso.get().load(model.iconUrl)
        }.apply {
            placeholder(R.drawable.ic_unknown_currency)
            error(R.drawable.ic_unknown_currency)
            into(currencyImg)
        }

        txtCurrencyIso.text = model.iso
        txtCurrencyName.text = model.name
        editCurrencyValue.setText(model.value)
        setSelected(model.isBase)
    }

    fun setValue(model: CurrencyUiModel) {
        if (editCurrencyValue.text.isEmpty() && model.isBase || editCurrencyValue.text.toString() == model.value)
            return

        editCurrencyValue.setTextKeepState(model.value)
    }

    fun setSelected(isSelected: Boolean) {
        if (isSelected) {
            editCurrencyValue.requestFocus()
            itemView.setBackgroundResource(R.color.selected)
            itemView.elevation = 2f
        } else {
            itemView.setBackgroundResource(R.color.item_background)
            itemView.elevation = 1f
        }
    }

    private inner class ValueTextWatcher(
        private val textChangesObservable: PublishSubject<Pair<Int, CharSequence>>
    ) : TextWatcher {

        override fun afterTextChanged(s: Editable) {}

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            textChangesObservable.onNext(Pair(adapterPosition, s))
        }
    }

}