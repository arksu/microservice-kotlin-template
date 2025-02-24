package com.company.service

import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Single(createdAtStart = true)
class BarService : KoinComponent {
    private val fooService: FooService by inject()

    fun some() {
        println("some of bar")
        fooService.foo()
    }
}