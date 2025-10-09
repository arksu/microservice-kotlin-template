package com.company.util

import io.ktor.server.application.*

fun ApplicationEnvironment.getValueString(name: String, default: String? = null): String {
    return this.config.propertyOrNull(name)?.getString() ?: default ?: throw RuntimeException("Missing string '$name' property")
}

fun ApplicationEnvironment.getValueInt(name: String, default: Int? = null): Int {
    return this.config.propertyOrNull(name)?.getString()?.toInt() ?: default ?: throw RuntimeException("Missing int '$name' property")
}

fun ApplicationEnvironment.getList(name: String): List<String> {
    return this.config.propertyOrNull(name)?.getList() ?: throw RuntimeException("Missing list '$name' property")
}