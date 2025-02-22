package com.company.config

import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.requestvalidation.*

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        jackson {
        }
    }

    install(RequestValidation) {
        validate<Customer> { customer ->
            if (customer.id <= 0)
                ValidationResult.Invalid("A customer ID should be greater than 0")
            else ValidationResult.Valid
        }
    }

}