package com.company.config

import com.company.service.HelloService
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    install(RequestValidation) {
        validate<String> { bodyText ->
            if (!bodyText.startsWith("Hello"))
                ValidationResult.Invalid("Body text should start with 'Hello'")
            else ValidationResult.Valid
        }
    }

    val service by inject<HelloService>()

    routing {
        get("/") {
            call.respond(service.hello())
        }

        get("/kafka") {
            service.produceKafkaMessage()
            call.respond("ok")
        }
    }

    routing {
        get("/json") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}
