package com.company.config

import io.ktor.server.application.*
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.dsl.module
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(
            module {
                single { environment }
            },
            AppModule().module,
            configureKafkaModule(),
            configureKtorfitModule(),
        )
    }
}

@Module
@ComponentScan("com.company")
class AppModule