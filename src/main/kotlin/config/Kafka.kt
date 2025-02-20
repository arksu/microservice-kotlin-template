package com.company.config

import io.ktor.server.application.*
import io.ktor.server.config.*
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.koin.core.module.Module
import java.util.*

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

    val producers: Map<String, KafkaProducer<String, String>> = producerConfigs.mapValues { (_, conf) ->
        val props = Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaGlobalConfig.property("bootstrapServers").getList())
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, conf.property("keySerializer").getString())
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, conf.property("valueSerializer").getString())
            conf.propertyOrNull("acks")?.let {
                put(ProducerConfig.ACKS_CONFIG, it.getString())
            }
        }
        KafkaProducer(props)
    }

    println(producers)


    val module = org.koin.dsl.module {
        single { producers }
    }
    return module
}
