package com.company.service.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.koin.core.annotation.Qualifier
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent

@Single(createdAtStart = true)
@Qualifier(name = "cons1")
class Cons1Consumer : IConsumer, KoinComponent {

    override fun processMessage(record: ConsumerRecord<String, String>) {
        println("Consumed message: key=${record.key()}, value=${record.value()}, offset=${record.offset()}")
        println(record.value())
    }
}