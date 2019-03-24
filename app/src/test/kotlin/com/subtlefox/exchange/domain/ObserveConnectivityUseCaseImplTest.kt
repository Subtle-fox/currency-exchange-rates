package com.subtlefox.exchange.domain

import com.subtlefox.exchange.domain.implementation.ObserveConnectivityUseCaseImpl
import com.subtlefox.exchange.domain.repository.ConnectivityRepository
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Test

class ObserveConnectivityUseCaseImplTest {
    val repo = mockk<ConnectivityRepository>()
    val usecase = ObserveConnectivityUseCaseImpl(repo)

    @Test
    fun `should stream only distinct`() {
        every { repo.observeConnectivity() } returns Observable.just(false, true, true, false, false)

        val testObserver = TestObserver<Boolean>()
        usecase.stream().subscribe(testObserver)

        testObserver.assertValueCount(3)
    }
}