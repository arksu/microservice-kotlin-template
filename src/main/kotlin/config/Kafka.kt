package com.company.config

import com.company.service.gson
import io.ktor.server.application.*
import io.ktor.server.config.*
import kotlinx.coroutines.*
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.errors.WakeupException
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.ktor.ext.getKoin
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
    val consumersImplMap = HashMap<String, IConsumer>()
    consumerConfigs.forEach { (name, config) ->
        val consumerProps = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaGlobalConfig.property("bootstrapServers").getList())
            put(ConsumerConfig.GROUP_ID_CONFIG, kafkaGlobalConfig.property("groupId").getString())
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, config.property("keyDeserializer").getString())
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, config.property("valueDeserializer").getString())
            put(ConsumerConfig.ENABLE_METRICS_PUSH_CONFIG, "false")

            config.propertyOrNull("autoOffsetReset")?.let {
                put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, it.getString())
            }
        }
        // TODO int deserializer
        val kafkaConsumer = KafkaConsumer<String, String>(consumerProps)
        kafkaConsumer.subscribe(listOf(config.property("topic").getString()))

        val job = launch(Dispatchers.IO) {
            try {
                while (isActive) {
                    val records = kafkaConsumer.poll(Duration.ofMillis(100))
                    if (!records.isEmpty) {
                        val consumerImpl = consumersImplMap.computeIfAbsent(name) {
                            getKoin().get<IConsumer>(qualifier = named(name))
                        }
                        for (record in records) {
                            consumerImpl.processMessage(record)
                        }
                    }
                }
            } catch (_: WakeupException) {
            } finally {
                kafkaConsumer.close()
            }
        }
        consumersMap[name] = kafkaConsumer
        jobsMap[name] = job
    }

    val producers: Map<String, KafkaProducer<Any, String>> = producerConfigs.mapValues { (_, config) ->
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

    // close all consumers and producers on application shutdown
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

suspend fun KafkaProducer<Any, String>.asyncSend(topic: String, key: UUID, message: Any): RecordMetadata {
    val record: ProducerRecord<Any, String> = ProducerRecord(topic, key.toString(), gson.toJson(message))
    return asyncSend(record)
}

suspend fun KafkaProducer<Any, String>.asyncSend(topic: String, key: Any, message: Any): RecordMetadata {
    val record: ProducerRecord<Any, String> = ProducerRecord(topic, key, gson.toJson(message))
    return asyncSend(record)
}

suspend fun KafkaProducer<Any, String>.asyncSend(topic: String, key: String, message: String): RecordMetadata {
    val record: ProducerRecord<Any, String> = ProducerRecord(topic, key, message)
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

interface IConsumer {
    suspend fun processMessage(record: ConsumerRecord<String, String>)
}