package com.subtlefox.currencyrates.di.components

import com.subtlefox.currencyrates.di.FeatureScope
import com.subtlefox.currencyrates.di.modules.RatesBindingModule
import com.subtlefox.currencyrates.di.modules.RatesProviderModule
import com.subtlefox.currencyrates.presentation.RatesActivity
import dagger.Subcomponent

@FeatureScope
@Subcomponent(
    modules = [
        RatesBindingModule::class,
        RatesProviderModule::class
    ]
)
interface RatesComponent {
    fun inject(fragment: RatesActivity)
}