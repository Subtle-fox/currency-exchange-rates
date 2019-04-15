package com.subtlefox.currencyrates.di

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider

class ViewModelFactory
@Inject
constructor(
    private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return creators[modelClass]
            ?.let { it.get() as T }
            ?: throw IllegalStateException("No provider for " + modelClass.canonicalName)
    }

}
