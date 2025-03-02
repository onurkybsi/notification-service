package org.kybprototyping.notificationservice.adapter.scheduled

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.kybprototyping.notificationservice.domain.common.CoroutineDispatcherProvider
import org.kybprototyping.notificationservice.domain.common.ScheduledJob
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.support.CronTrigger

@EnableScheduling
@Configuration
@ConditionalOnProperty(
    value = ["scheduled.enabled"],
    havingValue = "true",
)
internal class SpringConfiguration {
    @Bean
    fun jobScheduler(
        @Value("\${scheduled.pool-size}") poolSize: Int,
        coroutineDispatcherProvider: CoroutineDispatcherProvider,
        jobs: List<ScheduledJob>,
    ): ThreadPoolTaskScheduler {
        val scheduledJobThreadPool = ThreadPoolTaskScheduler().also {
            it.threadNamePrefix = "scheduled-"
            it.poolSize = poolSize
        }
        scheduledJobThreadPool.initialize()

        val jobsScope = CoroutineScope(scheduledJobThreadPool.asCoroutineDispatcher() + SupervisorJob())
        jobs.forEach { job -> scheduledJobThreadPool.schedule({ jobsScope.launch{ job.execute() } }, CronTrigger(job.cron)) }

        return scheduledJobThreadPool
    }
}
