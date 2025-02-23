package com.company.config

import dev.nesk.akkurate.ktor.server.Akkurate
import dev.nesk.akkurate.ktor.server.registerValidator
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.requestvalidation.*


fun Application.configureSerialization() {
    install(Akkurate)
    install(ContentNegotiation) {
        jackson {
        }
    }

    install(RequestValidation) {
        registerValidator(validateCustomer)
//        validate<Customer> { customer ->
//            if (customer.id <= 0)
//                ValidationResult.Invalid("A customer ID should be greater than 0")
//            else ValidationResult.Valid
//        }
    }

}