package com.company.config

import dev.nesk.akkurate.ktor.server.Akkurate
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*


fun Application.configureSerialization() {
    install(Akkurate)
    install(ContentNegotiation) {
        jackson {
        }
    }
}