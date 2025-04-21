package com.company.service

import org.koin.core.annotation.Single

@Single(createdAtStart = true)
class FooService(
    private val barService: BarService
) {

    fun some2() {
        barService.some()
    }

    fun foo() {
        println("foo")
    }
}