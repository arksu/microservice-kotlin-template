package com.company.config

import com.company.service.gson
import io.ktor.server.application.*
import io.ktor.server.config.*
import kotlinx.coroutines.*
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.errors.WakeupException
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import java.time.Duration
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun ApplicationConfig.getKafkaGlobalConfig(): ApplicationConfig {
    return this.config("kafka")
}

fun ApplicationConfig.getKafkaProducerConfigs(): Map<String, ApplicationConfig> {
    val producersConfig = this.config("kafka.producers")
    val producersMap = mutableMapOf<String, ApplicationConfig>()
    for (name in producersConfig.toMap().keys) {
        val conf = producersConfig.config(name)
        producersMap[name] = conf
    }
    return producersMap
}

fun ApplicationConfig.getKafkaConsumerConfigs(): Map<String, ApplicationConfig> {
    val consumersConfig = this.config("kafka.consumers")
    val consumersMap = mutableMapOf<String, ApplicationConfig>()
    for (name in consumersConfig.toMap().keys) {
        val conf = consumersConfig.config(name)
        consumersMap[name] = conf
    }
    return consumersMap
}

fun Application.configureKafkaModule(): Module {
    val kafkaGlobalConfig = environment.config.getKafkaGlobalConfig()
    val producerConfigs = environment.config.getKafkaProducerConfigs()
    val consumerConfigs = environment.config.getKafkaConsumerConfigs()

    val jobsMap = HashMap<String, Job>()
    val consumersMap = HashMap<String, KafkaConsumer<String, String>>()
    consumerConfigs.forEach { (name, config) ->
        val consumerProps = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaGlobalConfig.property("bootstrapServers").getList())
            put(ConsumerConfig.GROUP_ID_CONFIG, kafkaGlobalConfig.property("groupId").getString())
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, config.property("keyDeserializer").getString())
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, config.property("valueDeserializer").getString())

            config.propertyOrNull("autoOffsetReset")?.let {
                put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, it.getString())
            }
        }
        val consumer = KafkaConsumer<String, String>(consumerProps)
        consumer.subscribe(listOf(config.property("topic").getString()))

        val job = launch(Dispatchers.IO) {
            try {
                while (isActive) {
                    val records = consumer.poll(Duration.ofMillis(100))
                    for (record in records) {
                        println("Consumed message: key=${record.key()}, value=${record.value()}, offset=${record.offset()}")
                        // TODO Add any processing logic here.
                    }
                }
            } catch (_: WakeupException) {
            } finally {
                consumer.close()
            }
        }
        consumersMap[name] = consumer
        jobsMap[name] = job
    }

    val producers: Map<String, KafkaProducer<String, String>> = producerConfigs.mapValues { (_, config) ->
        val props = Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaGlobalConfig.property("bootstrapServers").getList())
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, config.property("keySerializer").getString())
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, config.property("valueSerializer").getString())
            config.propertyOrNull("acks")?.let {
                put(ProducerConfig.ACKS_CONFIG, it.getString())
            }
        }
        KafkaProducer(props)
    }

    // close all consumers on application shutdown
    monitor.subscribe(ApplicationStopping) {
        consumersMap.entries.forEach { (name, consumer) ->
            consumer.wakeup()
            runBlocking {
                jobsMap[name]?.cancelAndJoin()
            }
        }
        producers.values.forEach { producer ->
            producer.close()
        }
    }

    val module = org.koin.dsl.module {
        producers.entries.forEach { (k, p) ->
            single(qualifier = named(k)) { p }
        }
    }
    return module
}

suspend fun KafkaProducer<String, String>.asyncSend(topic: String, key: UUID, message: Any): RecordMetadata {
    val record = ProducerRecord(topic, key.toString(), gson.toJson(message))
    return asyncSend(record)
}

suspend fun KafkaProducer<String, String>.asyncSend(topic: String, key: String, message: Any): RecordMetadata {
    val record = ProducerRecord(topic, key, gson.toJson(message))
    return asyncSend(record)
}

suspend fun <K, V> KafkaProducer<K, V>.asyncSend(record: ProducerRecord<K, V>): RecordMetadata {
    return withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { cont ->
            this@asyncSend.send(record) { metadata, exception ->
                if (exception == null) {
                    cont.resume(metadata) // Successfully sent
                } else {
                    cont.resumeWithException(exception) // Handle error
                }
            }
        }
    }
}