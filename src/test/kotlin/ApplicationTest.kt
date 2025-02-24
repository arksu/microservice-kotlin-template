package com.company

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        environment {
            config = ApplicationConfig("application.yaml")
        }
        application {
//            module()
        }
        client.get("/health").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

}
