package com.company.service

import com.google.gson.GsonBuilder
import org.koin.core.component.KoinComponent

class SerializationService : KoinComponent {
    private val gson = GsonBuilder()
        .create()

    fun writeValueAsString(value: Any): String {
        return gson.toJson(value)
    }
}