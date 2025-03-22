package com.company.service.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord

interface IConsumer {
    fun processMessage(record: ConsumerRecord<String, String>)
}