package io.github.elkhoudiry.realtime.kafka

import io.github.elkhoudiry.realtime.kafka.model.TopicValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse
import java.nio.file.Files
import java.util.Properties

class KafkaStream(
    private val topic: String = "connect-events",
    private val applicationId: String = "connect-events",
    private val brokerHostName: String = "localhost",
    private val brokerPort: Int = 9092
) {
    private fun initConfiguration(): Properties {
        val configuration = Properties()

        configuration[StreamsConfig.APPLICATION_ID_CONFIG] = applicationId
        configuration[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "$brokerHostName:$brokerPort"
        configuration[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass.name
        configuration[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String().javaClass.name
        configuration[StreamsConfig.STATE_DIR_CONFIG] = Files
            .createTempDirectory("$applicationId-state")
            .toAbsolutePath()
            .toString()
        configuration[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"

        return configuration
    }

    fun <K, V> listen(): Flow<TopicValue<K, V>> {
        val flow = MutableSharedFlow<TopicValue<K, V>>()
        val topology: Topology
        val builder = StreamsBuilder()
        val textLines = builder.stream<K, V>(topic)

        textLines.foreach { key, value ->
            runBlocking { flow.emit(TopicValue(key, value)) }
        }

        topology = builder.build()
        val streams = KafkaStreams(topology, initConfiguration())
        streams.setUncaughtExceptionHandler { e: Throwable? ->
            e?.printStackTrace()
            StreamThreadExceptionResponse.REPLACE_THREAD
        }

        return flow
            .onStart {
                streams.start()
            }
            .onCompletion {
                streams.close()
            }
    }
}