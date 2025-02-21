package com.company.service

import com.company.config.Database
import com.company.config.asyncSend
import com.company.config.toFlux
import com.company.config.toMono
import com.company.jooq.tables.references.USERS
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.apache.kafka.clients.producer.KafkaProducer
import org.jooq.impl.DSL
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.util.*

class HelloService(
    private val database: Database,
) : KoinComponent {

    private val producer: KafkaProducer<Any, String> by inject(qualifier = named("stringProducer"))
    private val producer2: KafkaProducer<Any, String> by inject(qualifier = named("intProducer"))

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
        producer2.asyncSend("topic2", 12, dto)
    }

    data class UserDTO(
        val id: Long,
        val name: String,
    )

    data class KafkaDTO(
        val id: UUID,
        val payload: String,
    )
}