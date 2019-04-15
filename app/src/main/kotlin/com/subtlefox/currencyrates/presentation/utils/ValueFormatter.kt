package com.subtlefox.currencyrates.presentation.utils

import android.widget.EditText
import java.math.BigDecimal
import java.math.RoundingMode

internal fun String.toBigDecimalOrZero(): BigDecimal {
    return try {
        toBigDecimal()
    } catch (e: NumberFormatException) {
        BigDecimal.ZERO
    }
}

internal fun BigDecimal.toUiString(): String {
    if (compareTo(BigDecimal.ZERO) == 0) {
        return ""
    }

    val uiString = setScale(4, RoundingMode.HALF_EVEN)
        .stripTrailingZeros()
        .toPlainString()

    if (uiString.startsWith('.')) {
        return "0$uiString"
    }

    return uiString
}

internal fun EditText.postFormatValue() {
    val value = text.toString().toBigDecimalOrZero()
    setText(value.toUiString())
}