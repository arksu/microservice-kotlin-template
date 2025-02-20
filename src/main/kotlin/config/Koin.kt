package com.company.config

import com.company.service.HelloService
import io.ktor.server.application.*
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.ktor.plugin.Koin

fun Application.configureKoin() {
    install(Koin) {
        val module = org.koin.dsl.module {
            single { environment }
            singleOf(::Database) {
                createdAtStart()
            }
            singleOf(::HelloService)
        }
        modules(module)
    }

}