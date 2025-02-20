package com.company.config

import io.ktor.server.application.*
import io.ktor.server.config.*
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import java.util.*

data class ProducerConfigProps(
    val keySerializer: String,
    val valueSerializer: String,
    val topic: String
)

data class ConsumerConfigProps(
    val keyDeserializer: String,
    val valueDeserializer: String,
    val autoOffsetReset: String,
    val topic: String
)

data class KafkaGlobalConfig(
    val bootstrapServers: List<String>,
    val groupId: String
)

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

fun Application.configureKafka() {
    val kafkaGlobalConfig = environment.config.getKafkaGlobalConfig()
    val producerConfigs = environment.config.getKafkaProducerConfigs()
    val consumerConfigs = environment.config.getKafkaConsumerConfigs()

    val producers: Map<String, KafkaProducer<String, String>> = producerConfigs.mapValues { (_, conf) ->
        val props = Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaGlobalConfig.property("bootstrapServers").getList())
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, conf.property("keySerializer").getString())
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, conf.property("valueSerializer").getString())
        }
        KafkaProducer<String, String>(props)
    }

    println(consumerConfigs)
}
