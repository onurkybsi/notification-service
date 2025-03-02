package org.kybprototyping.notificationservice.domain.common

/**
 * Represents the Notification Service's scheduled jobs.
 */
interface ScheduledJob {
    /**
     * Executes a particular background job.
     */
    suspend fun execute()

    /**
     * Cron expression that specifies the execution schedule of the job.
     *
     * Based on this value [execute] should be executed.
     */
    val cron: String
}
