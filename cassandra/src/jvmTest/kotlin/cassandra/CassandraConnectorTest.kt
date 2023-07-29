package cassandra

import cassandra.error.CassandraNotInitializedException
import com.datastax.oss.driver.api.core.CqlSession
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.cassandraunit.CQLDataLoader
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs


class CassandraConnectorTest {
    private lateinit var cassandra: CassandraConnector

    @BeforeTest
    fun setup() {
        Thread { EmbeddedCassandraServerHelper.startEmbeddedCassandra(5000) }.start()
        Thread.sleep(5000)
        cassandra = CassandraConnector()
    }

    @AfterTest
    fun tearDown() {
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
        runBlocking {
            try {
                cassandra.close()
            }catch (ex: Exception){

            }
        }
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