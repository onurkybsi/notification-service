package org.kybprototyping.notificationservice.domain.scheduled.servicetask

import arrow.core.Either
import kotlinx.coroutines.*
import org.apache.logging.log4j.kotlin.cachedLoggerOf
import org.kybprototying.notificationservice.common.Failure
import org.kybprototying.notificationservice.common.TimeUtils
import org.kybprototyping.notificationservice.domain.common.CoroutineDispatcherProvider
import org.kybprototyping.notificationservice.domain.common.ScheduledJob
import org.kybprototyping.notificationservice.domain.model.ServiceTask
import org.kybprototyping.notificationservice.domain.model.ServiceTaskStatus.IN_PROGRESS
import org.kybprototyping.notificationservice.domain.model.ServiceTaskStatus.PENDING
import org.kybprototyping.notificationservice.domain.model.ServiceTaskStatus.ERROR
import org.kybprototyping.notificationservice.domain.model.ServiceTaskType
import org.kybprototyping.notificationservice.domain.port.ServiceTaskRepositoryPort

/**
 * Scheduled job that executes the async service tasks that are persistent in the form of [ServiceTask].
 *
 * This scheduled job should be executed every configured time([cron]) so that
 * the persistent service tasks([ServiceTask]) could be executed.
 */
internal class ServiceTaskExecutorJob(
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider,
    private val timeUtils: TimeUtils,
    private val serviceTaskRepositoryPort: ServiceTaskRepositoryPort,
    private val executors: Map<ServiceTaskType, ServiceTaskExecutor>,
): ScheduledJob {
    override suspend fun execute() {
        logger.info("Task execution is being started...")

        val now = timeUtils.nowAsOffsetDateTime()
        serviceTaskRepositoryPort
            .updateBy(
                statuses = executableStatuses,
                executionScheduledAt = now,
                statusToSet = IN_PROGRESS,
                executionScheduledAtToSet = null,
                executionStartedAtToSet = now,
            )
            .onLeft { logger.warn("Pending tasks couldn't be updated: $it") }
            .onRight { tasksToExecute ->
                val taskExecutions = mutableListOf<Deferred<Either<Failure, Unit>>>()
                withContext(coroutineDispatcherProvider.serviceTaskExecutorDispatcher) {
                    supervisorScope {
                        tasksToExecute.forEach { taskToExecute ->
                            if (!executors.containsKey(taskToExecute.type)) {
                                logger.warn("No executor found for ${taskToExecute.type}!")
                            } else {
                                taskExecutions.add(async { executors[taskToExecute.type]!!.execute(taskToExecute) })
                            }
                        }
                    }
                }

                val numOfSuccessfulExecutions = taskExecutions.filter { !it.isCancelled }.count { it.await().isRight() }
                val numOfGracefullyFailedExecutions = taskExecutions.filter { !it.isCancelled }.count { it.await().isLeft() }
                val numOfUnexpectedlyFailedExecutions = taskExecutions.count { it.isCancelled }
                logger.info("Task execution completed with $numOfSuccessfulExecutions success, " +
                        "$numOfGracefullyFailedExecutions graceful failure and $numOfUnexpectedlyFailedExecutions unexpected failure!")
            }
    }

    override val cron: String = "0/10 * * * * *"

    private companion object {
        private val logger = cachedLoggerOf(ServiceTaskExecutorJob::class.java)
        private val executableStatuses = listOf(PENDING, ERROR)
    }
}
