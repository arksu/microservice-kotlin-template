package com.company.scheduler

import com.company.config.SchedulerJob
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask
import com.github.kagkarlsson.scheduler.task.helper.Tasks
import com.github.kagkarlsson.scheduler.task.schedule.CronSchedule
import org.koin.core.annotation.Single

@Single
class HelloJob : SchedulerJob {
    override val task: RecurringTask<Void> = Tasks
        .recurring("hello_task2", CronSchedule("*/2 * * * * *"))
        .execute { _, _ ->
            println("hello job invoked")
        }
}