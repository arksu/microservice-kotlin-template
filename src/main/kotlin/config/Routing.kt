package com.company.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.company.service.HelloService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.*

fun Application.configureRouting() {
    val helloService by inject<HelloService>()

    install(RequestValidation) {
        validate<String> { bodyText ->
            if (!bodyText.startsWith("Hello"))
                ValidationResult.Invalid("Body text should start with 'Hello'")
            else ValidationResult.Valid
        }
    }

    routing {
        post("/login") {
            val permissions = listOf("USERS", "POP")
            val token = JWT.create()
                .withAudience("withAudience")
                .withIssuer("iss")
                .withClaim("username", "user1")
                .withExpiresAt(Date(System.currentTimeMillis() + 5))
                .withClaim("permissions", permissions)
                .sign(Algorithm.HMAC256("secret"))
            call.respond(hashMapOf("token" to token))
        }

//        authenticate {
            withAnyPermission("USERS") {
                get("/") {
                    call.respond(helloService.hello())
                }
            }
//        }

        get("/kafka") {
            helloService.produceKafkaMessage()
            call.respond("ok")
        }

        get("/json") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}
