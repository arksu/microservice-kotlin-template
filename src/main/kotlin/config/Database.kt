package com.company.config

import io.ktor.server.application.*
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.ConnectionFactoryOptions.*
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@Single(createdAtStart = true)
class Database(
    environment: ApplicationEnvironment
) : KoinComponent {

    //    val dialect = SQLDialect.MARIADB
    val dialect = SQLDialect.POSTGRES

    val url = environment.config.propertyOrNull("db.url")?.getString() ?: throw RuntimeException("db.url is not set")
    val user = environment.config.propertyOrNull("db.user")?.getString() ?: throw RuntimeException("db.user is not set")
    val password = environment.config.propertyOrNull("db.password")?.getString() ?: throw RuntimeException("db.password is not set")

    private fun createConnectionPool(): ConnectionPool {
        val r2dbcUrl = url.replace("jdbc", "r2dbc")
        val options = ConnectionFactoryOptions.parse(r2dbcUrl).mutate()
            .option(DRIVER, "pool")
//            .option(PROTOCOL, "mysql")
            .option(PROTOCOL, "postgresql")
            .option(USER, user)
            .option(PASSWORD, password)
            .option(CONNECT_TIMEOUT, Duration.ofSeconds(3))
            .option(STATEMENT_TIMEOUT, Duration.ofSeconds(20))
            .option(LOCK_WAIT_TIMEOUT, Duration.ofSeconds(5))
            .build()

        val connectionFactory = ConnectionFactories.get(options)
        val poolConfig = ConnectionPoolConfiguration.builder(connectionFactory)
            .maxSize(20)
            .initialSize(20)
            .maxIdleTime(Duration.ofMinutes(30))
            .build()
        return ConnectionPool(poolConfig)
    }

    private val connectionPool = createConnectionPool()

    private val flyway = Flyway.configure(this.javaClass.classLoader)
        .let { fl ->
            val schema = environment.config.propertyOrNull("db.schema")?.getString()
            if (!schema.isNullOrBlank()) {
                fl.schemas(schema)
            } else {
                fl
            }
        }
        .validateMigrationNaming(true)
        .executeInTransaction(true)
        .dataSource(url, user, password)
        .load()

    init {
        flyway.migrate()
    }

    /**
     * Acquires a connection from the pool, creates a jOOQ DSLContext, executes the provided block,
     * and ensures the connection is closed.
     */
    suspend fun <T> usingJooq(block: suspend (context: DSLContext) -> T): T {
        val connection = connectionPool.create().awaitSingle()
        try {
            val dsl = DSL.using(connection, dialect)
            return block(dsl)
        } finally {
            connection.close().awaitFirstOrNull()
        }
    }

    /**
     * Wraps the execution of the provided block in a transaction.
     * A connection is acquired, a transaction is begun, and a jOOQ DSLContext is created.
     * The transaction is committed if the block completes successfully; otherwise, it is rolled back.
     */
    suspend fun <T> transactionalJooq(block: suspend (context: DSLContext) -> T): T {
        val connection = connectionPool.create().awaitSingle()
        try {
            // Begin transaction
            connection.beginTransaction().awaitFirstOrNull()
            val dsl = DSL.using(connection, dialect)
            // Execute business logic in a transaction
            val result = block(dsl)
            // Commit if successful
            connection.commitTransaction().awaitFirstOrNull()
            return result
        } catch (e: Throwable) {
            // Roll back in case of error
            connection.rollbackTransaction().awaitFirstOrNull()
            throw e
        } finally {
            connection.close().awaitFirstOrNull()
        }
    }
}

fun <T> Publisher<T>.toMono(): Mono<T> = Mono.from(this)

fun <T> Publisher<T>.toFlux(): Flux<T> = Flux.from(this)
