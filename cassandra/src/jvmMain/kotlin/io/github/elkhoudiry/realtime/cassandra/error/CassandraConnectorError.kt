package io.github.elkhoudiry.realtime.cassandra.error

interface CassandraConnectorError

class CassandraNotInitializedException: Exception(), CassandraConnectorError {
    override val message: String
        get() = "Cassandra connector is not initialized."
}