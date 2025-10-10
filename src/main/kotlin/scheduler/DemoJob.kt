package com.company.scheduler

import com.company.config.SchedulerJob
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask
import com.github.kagkarlsson.scheduler.task.helper.Tasks
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.annotation.Single

@Single
class DemoJob : SchedulerJob {
    private val logger = KotlinLogging.logger { }

    override val task: RecurringTask<Void> = Tasks
        .recurring("demo1", FixedDelay.ofSeconds(5))
        .execute { _, _ ->
            logger.info { "demo job invoked" }
        }
}