package com.company.util

import io.ktor.server.application.*

fun ApplicationEnvironment.getValue(name: String): String {
    return this.config.propertyOrNull(name)?.getString() ?: throw RuntimeException("Missing '$name' property")
}

fun ApplicationEnvironment.getValueInt(name: String): Int {
    return this.config.propertyOrNull(name)?.getString()?.toInt() ?: throw RuntimeException("Missing '$name' property")
}

fun ApplicationEnvironment.getList(name: String): List<String> {
    return this.config.propertyOrNull(name)?.getList() ?: throw RuntimeException("Missing '$name' property")
}