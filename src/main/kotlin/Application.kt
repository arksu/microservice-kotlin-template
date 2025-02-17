package com.company

import com.company.config.configureKoin
import com.company.config.configureRouting
import com.company.config.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureKoin()
    configureSerialization()
//    configureMonitoring()
//    configureSecurity()
    configureRouting()
}
