package io.github.elkhoudiry.realtime.kafka

import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class KafkaStreamTest {

    @Test
    fun testStream() {
        val kafka = KafkaStream()
        runBlocking {
            kafka.listen<String, String>().collect {
                println("[LOG] collect: $it")
            }
        }
    }
}