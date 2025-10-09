package com.company.scheduler

import com.company.config.SchedulerJob
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask
import com.github.kagkarlsson.scheduler.task.helper.Tasks
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay
import org.koin.core.annotation.Single

@Single
class DemoJob : SchedulerJob {
    override val task: RecurringTask<Void> = Tasks
        .recurring("demo1", FixedDelay.ofSeconds(5))
        .execute { _, _ ->
            println("demo job invoked")
        }
}