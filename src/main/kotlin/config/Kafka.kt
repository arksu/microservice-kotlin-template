package com.company.config

import com.company.service.HelloService
import io.github.flaxoos.ktor.server.plugins.kafka.*
import io.ktor.client.*
import io.ktor.server.application.*


fun Application.configureKafka() {
    install(Kafka) {
        schemaRegistryUrl = "localhost:9092"
        val myTopic = TopicName.named("my-topic")
        val myTopic2 = TopicName.named("my-topic2")
//        topic(myTopic) {
//            partitions = 1
//            replicas = 1
//            configs {
//                messageTimestampType = MessageTimestampType.CreateTime
//            }
//        }
//        topic(myTopic2) {
//            partitions = 1
//            replicas = 1
//            configs {
//                messageTimestampType = MessageTimestampType.CreateTime
//            }
//        }
        common { // <-- Define common properties
            bootstrapServers = listOf("localhost:9092")
            retries = 1
            clientId = "my-client-id"
        }
        admin { } // <-- Creates an admin client
        producer { // <-- Creates a producer
            clientId = "my-client-id"
        }
        consumer { // <-- Creates a consumer
            groupId = "my-group-id"
            clientId = "my-client-id-override" //<-- Override common properties
        }
        consumerConfig {
            consumerRecordHandler(myTopic) { record ->
                // Do something with record
                println(record)
            }
            consumerRecordHandler(myTopic2) { record ->
                // Do something with record
                println(record)
            }
        }
        registerSchemas {
            using { // <-- optionally provide a client, by default CIO is used
                HttpClient()
            }

            HelloService.KafkaDTO::class at myTopic // <-- Will register schema upon startup
        }
    }
}
