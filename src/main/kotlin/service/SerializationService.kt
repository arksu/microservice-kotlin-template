package com.company.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent

val gson: Gson = GsonBuilder()
    .create()

@Single
class SerializationService : KoinComponent {
    private

    fun writeValueAsString(value: Any): String {
        return gson.toJson(value)
    }
}