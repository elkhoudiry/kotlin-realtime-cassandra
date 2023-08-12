import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

fun main() {
    val client = HttpClient(Java.create()) {
        install(ContentNegotiation) {
            json()
        }
    }

    runBlocking {
        client.delete {
            url("http://localhost:8081/subjects/connect-events-value")
        }
        client.delete {
            url("http://localhost:8083/connectors/connect-events")
        }
        client.delete {
            url("http://localhost:8083/connectors/connect-event")
        }
        client.post {
            this.url("http://localhost:8083/connectors")
            header("Content-Type", "application/json")
            setBody(getBody())
        }
    }

    runBlocking {
        val response = client.get("http://localhost:8083/connectors")

        println("[LOG] response: ${response.bodyAsText()}")
    }
}

private fun getBody(): JsonObject {
    return buildJsonObject {
        put("name", "connect-events")
        put(
            "config",
            buildJsonObject {
                put("tasks.max", 1)
                put(
                    "connector.class",
                    "com.datamountaineer.streamreactor.connect.cassandra.source.CassandraSourceConnector"
                )
                put("connect.cassandra.contact.points", "cassandra-seed")
                put("connect.cassandra.port", 9042)
                put("connect.cassandra.key.space", "ahmed")
                put("connect.cassandra.username", "cassandra")
                put("connect.cassandra.password", "cassandra")
                put("connect.cassandra.consistency.level", "ONE")
                put(
                    "connect.cassandra.kcql",
                    "INSERT INTO connect-events SELECT event_data, event_ts FROM events IGNORE " +
                        "event_ts PK event_ts WITHUNWRAP INCREMENTALMODE=TIMEUUID"
                )
            }
        )
    }
}