package com.subtlefox.currencyrates.domain

import io.reactivex.Scheduler

class Config(
    val computation: Scheduler,
    val intervalSec: Long,
    val delaySec: Long = 0L,
    val timeoutSec: Long = 10L,
    val maxConcurrency: Int = 10
)