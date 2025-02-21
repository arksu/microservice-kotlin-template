package com.company.service

import com.company.config.Database
import com.company.config.toFlux
import com.company.config.toMono
import com.company.jooq.tables.references.USERS
import com.company.util.asyncSend
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.apache.kafka.clients.producer.KafkaProducer
import org.jooq.impl.DSL
import org.koin.core.component.KoinComponent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class HelloService(
    private val database: Database,
    private val serializationService: SerializationService,
    producers: Map<String, KafkaProducer<String, String>>
) : KoinComponent {

    val map = ConcurrentHashMap<String, String>()

    private val producer = producers["prod1"] ?: throw RuntimeException("no required producer")

    suspend fun hello(): List<UserDTO> {
        val charset = ('a'..'z').toList()
        val name = (1..128)
            .map { charset.random() }
            .joinToString("")

        if (map.containsKey(name)) {
            println("collision")
            return listOf()
        }
        map.put(name, "1")

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

        producer.asyncSend("topic1", dto.id.toString(), serializationService.writeValueAsString(dto))
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