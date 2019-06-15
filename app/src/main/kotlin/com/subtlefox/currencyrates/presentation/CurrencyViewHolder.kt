package com.subtlefox.currencyrates.presentation

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.subtlefox.currencyrates.R
import com.subtlefox.currencyrates.presentation.utils.openKeyboard
import com.subtlefox.currencyrates.presentation.utils.postFormatValue
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
    private val clickListener = OnClickListener(onClickObserver)
    private val touchListener = ViewTouchListener(onClickObserver)
    private val focusListener = FocusChangeListener()

    private var isSelected = false

    init {
        itemView.setOnClickListener(clickListener)
        editCurrencyValue.setOnTouchListener(touchListener)
        editCurrencyValue.onFocusChangeListener = focusListener
    }

    fun bind(model: CurrencyUiModel) {
        txtCurrencyIso.text = model.iso
        txtCurrencyName.text = model.name
        editCurrencyValue.setTextKeepState(model.value)
        setSelected(model.isBase)
        loadImage(model.iconUrl)
    }

    fun setValue(value: String) {
        editCurrencyValue.setTextKeepState(value)
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

    private fun loadImage(iconUrl: String) {
        if (iconUrl.isEmpty()) {
            Picasso.get().load(R.drawable.ic_unknown_currency)
        } else {
            Picasso.get().load(iconUrl)
        }.apply {
            placeholder(R.drawable.ic_unknown_currency)
            error(R.drawable.ic_unknown_currency)
            into(currencyImg)
        }
    }

    private inner class ValueTextWatcher(
        private val textChangesObservable: PublishSubject<Pair<Int, CharSequence>>
    ) : TextWatcher {

        override fun afterTextChanged(s: Editable) {
            s.toString().run {
                if (equals(".")) {
                    editCurrencyValue.setText("0.")
                    editCurrencyValue.setSelection(2)
                }
            }
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (editCurrencyValue.hasFocus()) {
                textChangesObservable.onNext(Pair(adapterPosition, s))
            }
        }
    }


    private inner class OnClickListener(
        private val clickObserver: Observer<in Int>
    ) : View.OnClickListener {

        override fun onClick(v: View?) {
            if (!isSelected) {
                isSelected = true
                editCurrencyValue.requestFocus()
                clickObserver.onNext(adapterPosition)

                itemView.postDelayed({
                    editCurrencyValue.openKeyboard()
                    editCurrencyValue.addTextChangedListener(textWatcher)
                }, 150)
            }
        }

    }

    private inner class ViewTouchListener(
        private val clickObserver: Observer<in Int>
    ) : View.OnTouchListener {

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (MotionEvent.ACTION_UP == event.action && !isSelected) {
                isSelected = true
                clickObserver.onNext(adapterPosition)
                editCurrencyValue.addTextChangedListener(textWatcher)
            }
            return false
        }

    }

    private inner class FocusChangeListener : View.OnFocusChangeListener {

        override fun onFocusChange(v: View, hasFocus: Boolean) {
            if (!hasFocus) {
                editCurrencyValue.removeTextChangedListener(textWatcher)
                editCurrencyValue.postFormatValue()
                isSelected = false
            }
        }

    }

}