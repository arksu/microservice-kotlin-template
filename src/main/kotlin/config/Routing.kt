package com.company.config

import com.company.routes.api
import io.ktor.server.application.*
import io.ktor.server.resources.*

fun Application.configureRouting() {
    install(Resources)
    api()
}
