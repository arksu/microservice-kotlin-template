package com.company.config

import com.company.util.getValue
import com.company.util.getValueInt
import io.github.flaxoos.ktor.server.plugins.taskscheduling.TaskScheduling
import io.github.flaxoos.ktor.server.plugins.taskscheduling.TaskSchedulingConfiguration
import io.github.flaxoos.ktor.server.plugins.taskscheduling.managers.lock.redis.redis
import io.ktor.server.application.*

fun Application.configureScheduler() {
    install(TaskScheduling) {
        redis {
            host = environment.getValue("redis.host")
            port = environment.getValueInt("redis.port")
        }
        task1()
    }
}

private fun TaskSchedulingConfiguration.task1() {
    task {
        name = "Test1"
        task = { taskExecutionTime ->
            log.info("Test1 task is running: $taskExecutionTime")
        }
        kronSchedule = {
            seconds {
                from(0) every 3
            }
        }
    }
}