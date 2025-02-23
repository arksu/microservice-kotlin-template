package com.company.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.company.service.HelloService
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import dev.nesk.akkurate.annotations.Validate
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import java.util.*

fun Application.configureRouting() {
    val helloService by inject<HelloService>()

    routing {
        post("/login") {
            val permissions = listOf("USERS", "POP")
            val token = JWT.create()
                .withAudience("withAudience")
                .withIssuer("iss")
                .withClaim("username", "user1")
                .withExpiresAt(Date(System.currentTimeMillis() + 500000000))
                .withClaim("permissions", permissions)
                .sign(Algorithm.HMAC256("secret"))
            call.respond(hashMapOf("token" to token))
        }

        authenticate {
            withAnyPermission("USERS") {
                get("/") {
                    call.respond(helloService.hello())
                }
            }
        }

        get("/kafka") {
            helloService.produceKafkaMessage()
            call.respond("ok")
        }

        get("/json") {
            call.respond(mapOf("hello" to "world"))
        }

        post("/post") {
            val customer =  call.receive<Customer>()
            call.respond(customer)
        }
    }
}

@Validate
@Serializable
//@JsonIgnoreProperties(ignoreUnknown = true)
data class Customer(
    val id: Int,
    val firstName: String,
    val lastName: String
)
