package com.company.error

import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

data class ErrorResponseDTO(
    val error: String
)

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
    exception<NotFoundException> { call, e ->
        call.respond(HttpStatusCode.NotFound, ErrorResponseDTO(e.message!!))
    }
    exception<ClientRequestException> { call, e ->
        call.respond(e.response.status, ErrorResponseDTO(e.response.bodyAsText()))
    }
    exception<BadRequestException> { call, e ->
        call.respond(HttpStatusCode.BadRequest, ErrorResponseDTO(e.message ?: "Bad request"))
    }
    exception<RequestValidationException> { call, cause ->
        call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString())
    }
    exception<dev.nesk.akkurate.ValidationResult.Exception> { call, e ->
        call.respond(HttpStatusCode.UnprocessableEntity, e.violations)
    }
    exception<Exception> { call, e ->
        call.application.log.error("${e.message}", e)
        call.respond(HttpStatusCode.InternalServerError, ErrorResponseDTO(e.message ?: e.javaClass.simpleName))
    }
}