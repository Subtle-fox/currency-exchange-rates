package com.subtlefox.exchange.di.components

import com.subtlefox.exchange.di.FeatureScope
import com.subtlefox.exchange.di.modules.RatesBindingModule
import com.subtlefox.exchange.di.modules.RatesProviderModule
import com.subtlefox.exchange.presentation.RatesActivity
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