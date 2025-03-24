package com.company.config

import com.company.service.createExampleApi
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import org.koin.core.module.Module
import org.koin.dsl.module

fun Application.configureKtorfitModule(): Module {
    val httpClient = HttpClient {
        install(ContentNegotiation) {
            jackson()
        }
    }

    val ktorfit = Ktorfit.Builder()
        .httpClient(httpClient)
        .build()

    return module {
        single { httpClient }
        single { ktorfit }
        single { ktorfit.createExampleApi() }
    }
}