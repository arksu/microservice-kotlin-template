package com

import com.config.configureKoin
import com.config.configureRouting
import com.config.configureSerialization
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureKoin()
    configureSerialization()
//    configureMonitoring()
//    configureSecurity()
    configureRouting()
}
