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
    exception<AuthorizationException> { call, e ->
        call.respondText(status = HttpStatusCode.Forbidden, text = e.message ?: "Forbidden")
    }
    exception<AuthenticationException> { call, e ->
        call.respondText(status = HttpStatusCode.Unauthorized, text = e.message ?: "Unauthorized")
    }
    exception<BadRequestException> { call, e ->
        call.respond(HttpStatusCode.BadRequest, e.message!!)
    }
    exception<RequestValidationException> { call, cause ->
        call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString())
    }
    exception<Throwable> { call, e ->
        call.respond(HttpStatusCode.InternalServerError, e.message ?: e.javaClass.simpleName)
    }
}