package com.company.config

import com.company.util.getValueInt
import com.company.util.getValueString
import io.ktor.server.application.*
import org.koin.core.module.Module
import org.koin.dsl.module
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config

fun Application.configureRedisModule(): Module {

    val host = environment.getValueString("redis.host")
    val port = environment.getValueInt("redis.port")
    val connectionPoolSize = environment.getValueInt("redis.connectionPoolSize", 5)
    val connectionMinimumIdleSize = environment.getValueInt("redis.connectionMinimumIdleSize", 2)
    // Error will be thrown if Valkey or Redis command can’t be sent to server after retryAttempts. But if it was sent successfully then timeout will be started.
    val retryAttempts = environment.getValueInt("redis.retryAttempts", 3)
    // Timeout in milliseconds during connecting to any Valkey or Redis server
    val connectTimeout = environment.getValueInt("redis.connectTimeout", 10000)
    // Valkey or Redis server response timeout in milliseconds. Starts countdown after a Valkey or Redis command is successfully sent.
    val commandTimeout = environment.getValueInt("redis.commandTimeout", 3000)
    /*
    if a pooled connection is not used for a timeout time and the current connections amount is bigger
    than the minimum idle connections pool size,
    then it will be closed and removed from the pool. Value in milliseconds.
     */
    val idleConnectionTimeout = environment.getValueInt("redis.idleConnectionTimeout", 10000)
    val pingConnectionInterval = environment.getValueInt("redis.pingConnectionInterval", 10000)
    val dnsMonitoringInterval = environment.getValueInt("redis.dnsMonitoringInterval", 5000)
    val subscriptionConnectionMinimumIdleSize = environment.getValueInt("redis.subscriptionConnectionMinimumIdleSize", 1)
    val subscriptionConnectionPoolSize = environment.getValueInt("redis.subscriptionConnectionPoolSize", 5)

    val config = Config().apply {
        useSingleServer().apply {
            // Connection pool settings
            this.connectionMinimumIdleSize = connectionMinimumIdleSize
            this.connectionPoolSize = connectionPoolSize
            address = "redis://$host:$port"

            // Reconnection settings
            this.retryAttempts = retryAttempts
            this.connectTimeout = connectTimeout

            // Connection health and maintenance
            this.idleConnectionTimeout = idleConnectionTimeout
            this.pingConnectionInterval = pingConnectionInterval

            // Keep alive configuration - enables TCP keepalive using system defaults
            // This ensures idle connections are kept alive and network issues are detected quickly
            // System TCP keepalive settings can be configured at OS level if needed
            setKeepAlive(true)
            setTcpKeepAliveCount(5)
            setTcpKeepAliveIdle(10)
            setTcpKeepAliveInterval(5)

            // DNS monitoring (for production stability)
            this.dnsMonitoringInterval = dnsMonitoringInterval.toLong()

            // Subscription connection pool size
            this.subscriptionConnectionMinimumIdleSize = subscriptionConnectionMinimumIdleSize
            this.subscriptionConnectionPoolSize = subscriptionConnectionPoolSize

            // Command timeout
            timeout = commandTimeout
        }
    }

    val redissonClient: RedissonClient = try {
        Redisson.create(config)
    } catch (e: Exception) {
        // Log the error and rethrow to prevent silent failures
        environment.log.error("Failed to create Redis connection: ${e.message}")
        throw e
    }

    return module {
        single { redissonClient }
    }
}