package com.company.config

import com.company.util.getValueInt
import com.company.util.getValueString
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions.*
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.dsl.module
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import javax.sql.DataSource

fun databaseModule(environment: ApplicationEnvironment) = module {
    val dbHost = System.getenv("DB_HOST")
    val dbPort = System.getenv("DB_PORT")
    val dbDatabase = System.getenv("DB_NAME")
    val dbUser = System.getenv("DB_USERNAME") ?: environment.getValueString("db.user")
    val dbPassword = System.getenv("DB_PASSWORD") ?: environment.getValueString("db.password")
    val dbSchema = System.getenv("DB_SCHEMA") ?: environment.getValueString("db.schema", "public")

    val url = if (dbHost != null && dbPort != null && dbDatabase != null) "jdbc:postgresql://$dbHost:$dbPort/$dbDatabase"
    else environment.getValueString("db.url")

    val r2dbcUrl = url.replace("jdbc", "r2dbc")

    val poolMaxSize = environment.getValueInt("db.poolMaxSize", 20)
    val poolInitialSize = environment.getValueInt("db.poolInitialSize", 20)
    val maxIdleTime = environment.getValueInt("db.maxIdleTime", 30)
    val connectionTimeout = environment.getValueInt("db.connectTimeout", 3)
    val statementTimeout = environment.getValueInt("db.statementTimeout", 20)
    val lockWaitTimeout = environment.getValueInt("db.lockWaitTimeout", 5)

    single<ConnectionPool> {
        val options = parse(r2dbcUrl).mutate()
            .option(DRIVER, "pool")
//            .option(PROTOCOL, "mysql")
            .option(PROTOCOL, "postgresql")
            .option(USER, dbUser)
            .option(PASSWORD, dbPassword)
            .option(CONNECT_TIMEOUT, Duration.ofSeconds(connectionTimeout.toLong()))
            .option(STATEMENT_TIMEOUT, Duration.ofSeconds(statementTimeout.toLong()))
            .option(LOCK_WAIT_TIMEOUT, Duration.ofSeconds(lockWaitTimeout.toLong()))
            .build()

        val connectionFactory = ConnectionFactories.get(options)
        val poolConfig = ConnectionPoolConfiguration.builder(connectionFactory)
            .maxSize(poolMaxSize)
            .initialSize(poolInitialSize)
            .maxIdleTime(Duration.ofSeconds(maxIdleTime.toLong()))
            .build()
        ConnectionPool(poolConfig)
    }

    single<DataSource> {
        val config = HikariConfig().apply {
            jdbcUrl = url
            username = dbUser
            password = dbPassword
            schema = dbSchema
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 2
        }
        HikariDataSource(config)
    }

    single<Flyway> {
        Flyway.configure()
            .dataSource(get<DataSource>())
            .schemas(dbSchema)
            .locations("classpath:db/migration")
            .load()
    }
}

@Single(createdAtStart = true)
class Database(
    val connectionPool: ConnectionPool
) : KoinComponent {

    //    val dialect = SQLDialect.MARIADB
    val dialect = SQLDialect.POSTGRES

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
