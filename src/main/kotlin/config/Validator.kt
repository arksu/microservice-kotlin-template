package com.company.config

import com.company.service.validateCustomer
import dev.nesk.akkurate.ktor.server.registerValidator
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*

fun Application.configureValidation() {
    install(RequestValidation) {
        registerValidator(validateCustomer)
    }
}