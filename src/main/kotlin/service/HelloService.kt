package com.company.service

import com.company.config.Database
import com.company.config.asyncSend
import com.company.config.toFlux
import com.company.config.toMono
import com.company.jooq.tables.references.USERS
import com.company.service.validation.accessors.firstName
import com.company.service.validation.accessors.id
import com.company.service.validation.accessors.lastName
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.jensklingenberg.ktorfit.http.GET
import dev.nesk.akkurate.annotations.Validate
import dev.nesk.akkurate.constraints.builders.hasLengthGreaterThan
import dev.nesk.akkurate.constraints.builders.isGreaterThan
import dev.nesk.akkurate.constraints.builders.isNotEmpty
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.apache.kafka.clients.producer.KafkaProducer
import org.jooq.impl.DSL
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.util.*

@Single
class HelloService(
    private val database: Database,
    private val exampleApi: ExampleApi,
    private val fooService: FooService,
) : KoinComponent {

    private val producer: KafkaProducer<Any, String> by inject(qualifier = named("stringProducer"))

    private val charset = ('a'..'z').toList()

    suspend fun hello(): List<UserDTO> {
        val name = (1..128)
            .map { charset.random() }
            .joinToString("")

        val list = database.transactionalJooq { context ->

            context.insertInto(USERS)
                .set(USERS.NAME, name)
                .toMono()
                .awaitFirstOrNull()

            context.selectFrom(USERS)
                .where(DSL.lower(USERS.NAME).like("%g%d%w%"))
                .orderBy(USERS.CREATED.desc())
                .limit(5)
                .toFlux()
                .collectList()
                .awaitSingle()
        }
        return list.map {
            UserDTO(it.id!!, it.name)
        }
    }

    suspend fun produceKafkaMessage() {
        val dto = KafkaDTO(UUID.randomUUID(), "some data")

        producer.asyncSend("topic1", dto.id, dto)
    }

    data class UserDTO(
        val id: Long,
        val name: String,
    )

    data class KafkaDTO(
        val id: UUID,
        val payload: String,
    )

    fun foo() {
        fooService.some2()
    }

    suspend fun ktorfit(): List<Coffee> {
        return exampleApi.getCoffee()
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Coffee(
    val title: String,
    val ingredients: List<String>,
    val id: Int
)

interface ExampleApi {
    @GET("https://api.sampleapis.com/coffee/hot")
    suspend fun getCoffee(): List<Coffee>
}

@Validate
@JsonIgnoreProperties(ignoreUnknown = true)
data class Customer(
    val id: Int,
    val firstName: String,
    val lastName: String
)
val validateCustomer = dev.nesk.akkurate.Validator<Customer> {
    id.isGreaterThan(10)
    firstName.isNotEmpty()
    lastName.hasLengthGreaterThan(3)
}