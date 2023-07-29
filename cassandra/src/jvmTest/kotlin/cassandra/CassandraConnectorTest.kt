package cassandra

import cassandra.error.CassandraNotInitializedException
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

class CassandraConnectorTest {

    private lateinit var cassandra: CassandraConnector

    @BeforeTest
    fun setup() {
        cassandra = CassandraConnector()
    }

    @Test
    fun testUnInitialized() {
        try {
            cassandra.session
        } catch (ex: Exception) {
            assertIs<CassandraNotInitializedException>(ex)
        }
    }

    @Test
    fun connect() = runTest {
        cassandra.connect("localhost", 9042, "datacenter1")
        @Suppress("SENSELESS_COMPARISON")
        assert(cassandra.session != null)
        assert(!cassandra.session.isClosed)
    }

    @Test
    fun close() = runTest {
        cassandra.close()
    }
}