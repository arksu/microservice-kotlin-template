package com.company.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.koin.core.annotation.Single

val gson: Gson = GsonBuilder()
    .create()

@Single
class SerializationService(
    private val objectMapper: ObjectMapper
) {
    private

    fun writeValueAsString(value: Any): String {
        return gson.toJson(value)
    }
}