package com.subtlefox.currencyrates.presentation.utils

import androidx.core.content.ContextCompat
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

fun EditText.openKeyboard() {
    val imm = ContextCompat.getSystemService(context, InputMethodManager::class.java)
    imm?.showSoftInput(this, 0)
}

fun View.hideKeyboard() {
    val imm = ContextCompat.getSystemService(context, InputMethodManager::class.java)
    imm?.hideSoftInputFromWindow(windowToken, 0)
}