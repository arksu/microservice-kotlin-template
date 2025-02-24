package com.company.service

import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent

@Single(createdAtStart = true)
class FooService(
    private val barService: BarService
) : KoinComponent {

    fun some2() {
        barService.some()
    }

    fun foo() {
        println("foo")
    }
}