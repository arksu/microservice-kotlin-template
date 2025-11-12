package com.company.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import io.ktor.server.application.*
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.dsl.module
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    val objectMapper: ObjectMapper = jacksonMapperBuilder().build()

    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    objectMapper.registerModule(JavaTimeModule())

    install(Koin) {
        slf4jLogger()
        modules(
            module {
                single { environment }
                single { objectMapper }
            },
            AppModule().module,
            databaseModule(environment),
            dbScheduler,
            configureRedisModule(),
            configureKafkaModule(),
            configureKtorfitModule(),
        )
    }
}

@Module
@ComponentScan("com.company")
class AppModule