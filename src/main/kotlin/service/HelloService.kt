package com.company.service

import com.company.config.Database
import com.company.config.toFlux
import com.company.config.toMono
import com.company.jooq.tables.references.USERS
import com.sksamuel.avro4k.AvroNamespace
import com.sksamuel.avro4k.serializer.UUIDSerializer
import io.github.flaxoos.ktor.server.plugins.kafka.KafkaRecordKey
import io.github.flaxoos.ktor.server.plugins.kafka.components.toRecord
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.serialization.Serializable
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.jooq.impl.DSL
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class HelloService : KoinComponent {
    private val database: Database by inject()
    private val producer: KafkaProducer<KafkaRecordKey, GenericRecord> by inject()

    val map = ConcurrentHashMap<String, String>()

    suspend fun hello(): List<DTO> {
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
            DTO(it.id!!, it.name)
        }
    }

    suspend fun produceKafkaMessage() {
        val dto = KafkaDTO(
            UUID.randomUUID(), "fdsfds"
        )
        producer.send(ProducerRecord("my-topic", dto.id.toString(), dto.toRecord())).get(100, TimeUnit.SECONDS)
    }

    data class DTO(
        val id: Long,
        val name: String,
    )

    @Serializable
    @AvroNamespace("flaxoos.github.io.domain")
    data class KafkaDTO(
        @Serializable(with = UUIDSerializer::class)
        val id: UUID,
        val payload: String,
    )
}