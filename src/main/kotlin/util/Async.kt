package com.company.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun KafkaProducer<String, String>.asyncSend(topic: String, key: String, message: String): RecordMetadata {
    val record = ProducerRecord(topic, key, message)
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

