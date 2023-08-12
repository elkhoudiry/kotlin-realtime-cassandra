package io.github.elkhoudiry.realtime.kafka.model

data class TopicValue<K, V>(
    val key: K?,
    val value: V?
)