package com.company.scheduler

import com.company.config.SchedulerJob
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask
import com.github.kagkarlsson.scheduler.task.helper.Tasks
import com.github.kagkarlsson.scheduler.task.schedule.CronSchedule
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.annotation.Single

@Single
class HelloJob : SchedulerJob {
    private val logger = KotlinLogging.logger { }

    override val task: RecurringTask<Void> = Tasks
        .recurring("hello_task2", CronSchedule("*/2 * * * * *"))
        .execute { _, _ ->
            logger.info { "hello job invoked" }
        }
}