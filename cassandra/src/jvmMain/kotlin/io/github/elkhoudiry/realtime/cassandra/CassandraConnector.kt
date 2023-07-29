package io.github.elkhoudiry.realtime.cassandra

import io.github.elkhoudiry.realtime.cassandra.error.CassandraNotInitializedException
import com.datastax.oss.driver.api.core.CqlSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress

class CassandraConnector {
    @Volatile
    private var _session: CqlSession? = null
    val session: CqlSession
        get() {
            if (_session == null) throw CassandraNotInitializedException()
            return _session!!
        }


    suspend fun connect(
        hostname: String,
        port: Int,
        localDatacenter: String
    ) = withContext(currentCoroutineContext() + Dispatchers.IO) {
        _session = CqlSession
            .builder()
            .addContactPoint(InetSocketAddress(hostname, port))
            .withLocalDatacenter(localDatacenter)
            .build()
    }

    suspend fun close() = withContext(currentCoroutineContext() + Dispatchers.IO) {
        if (_session != null && !session.isClosed) {
            session.close()
            _session = null
        }
    }
}