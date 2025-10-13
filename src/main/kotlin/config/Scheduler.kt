package com.company.config

import com.github.kagkarlsson.scheduler.Scheduler
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask
import org.koin.dsl.module
import java.time.Duration
import javax.sql.DataSource

val dbScheduler = module {
    single<Scheduler>(createdAtStart = true) {
        val dataSource: DataSource = get<DataSource>()
        val jobs: List<SchedulerTask> = getAll()
        val tasks = jobs.map { it.task }

        Scheduler.create(dataSource)
            .startTasks(tasks)
            .threads(4)
            .pollingInterval(Duration.ofSeconds(1))
            .build()
    }
}

interface SchedulerTask {
    val task: RecurringTask<*>
}