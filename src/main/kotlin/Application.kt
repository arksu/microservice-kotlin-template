package com.company

import com.company.config.*
import com.company.error.configureExceptionHandler
import com.github.kagkarlsson.scheduler.Scheduler
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.flywaydb.core.Flyway
import org.koin.ktor.ext.inject
import org.redisson.api.RedissonClient

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureValidation()
    configureExceptionHandler()
    configureMonitoring()
    configureKoin()
    configureJwtSecurity()
    configureRouting()
    configureHealth()

    val flyway: Flyway by inject()
    val scheduler: Scheduler by inject()
    val redis: RedissonClient by inject()

    monitor.subscribe(ApplicationStarted) {
        flyway.migrate()
        scheduler.start()
    }

    monitor.subscribe(ApplicationStopping) {
        scheduler.stop()
        redis.shutdown()
    }
}
