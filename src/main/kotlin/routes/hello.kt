package com.company.routes

import com.company.service.Customer
import com.company.service.HelloService
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.hello() {
    val helloService by inject<HelloService>()

    routing {
        get<Articles> { article ->
            // Get all articles ...
            call.respondText("List of articles sorted starting from ${article.sort}")
        }

        post<Articles.New> {
            val req = call.receive<Article>()
            println(req)
            // Save an article ...
            call.respondText("An article is saved", status = HttpStatusCode.Created)
        }

        get("/kafka") {
            helloService.produceKafkaMessage()
            call.respond("ok")
        }

        get("/json") {
            call.respond(mapOf("hello" to "world"))
        }

        post("/post") {
            val customer = call.receive<Customer>()
            call.respond(customer)
        }

        get("/foo") {
            helloService.foo()
            call.respond(HttpStatusCode.NoContent)
        }

        get("ktorfit") {
            val resp = helloService.ktorfit()
            call.respond(resp)
        }
    }
}

@Resource("/articles")
class Articles(val sort: String? = "new") {

    @Resource("new")
    class New(val parent: Articles = Articles())

    @Resource("{id}")
    class Id(val parent: Articles = Articles(), val id: Long) {
        @Resource("edit")
        class Edit(val parent: Id)
    }
}

data class Article(
    val foo: Int,
    val some: String
)

