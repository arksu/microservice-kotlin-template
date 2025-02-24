package com.company

import com.company.config.*
import com.company.error.configureExceptionHandler
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureExceptionHandler()
    configureMonitoring()
    configureKoin()
    configureJwtSecurity()
    configureRouting()
    configureHealth()
}
