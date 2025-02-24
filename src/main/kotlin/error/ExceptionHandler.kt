package com.company.error

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureExceptionHandler() {
    install(StatusPages) {
        exceptionHandler()
    }
}

fun StatusPagesConfig.exceptionHandler() {
    exception<AuthenticationException> { call, e ->
        call.respondText(status = HttpStatusCode.Unauthorized, text = e.message ?: "Unauthorized")
    }
    exception<AuthorizationException> { call, e ->
        call.respondText(status = HttpStatusCode.Forbidden, text = e.message ?: "Forbidden")
    }
    exception<BadRequestException> { call, e ->
        call.respond(HttpStatusCode.BadRequest, e.message!!)
    }
    exception<RequestValidationException> { call, cause ->
        call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString())
    }
    exception<dev.nesk.akkurate.ValidationResult.Exception> { call, e ->
        call.respond(HttpStatusCode.UnprocessableEntity, e.violations)
    }
    exception<Exception> { call, e ->
        call.respond(HttpStatusCode.InternalServerError, e.message ?: e.javaClass.simpleName)
    }
}