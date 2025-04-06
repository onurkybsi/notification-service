package org.kybprototyping.notificationservice.domain.scheduled.servicetask

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.logging.log4j.kotlin.cachedLoggerOf
import org.kybprototyping.notificationservice.domain.common.ScheduledJob
import org.kybprototyping.notificationservice.domain.model.ServiceTask
import org.kybprototyping.notificationservice.domain.model.ServiceTaskStatus.*
import org.kybprototyping.notificationservice.domain.model.ServiceTaskType.SEND_EMAIL
import org.kybprototyping.notificationservice.domain.port.ServiceTaskPublisherPort
import org.kybprototyping.notificationservice.domain.port.ServiceTaskRepositoryPort
import org.kybprototyping.notificationservice.domain.port.TransactionalExecutor
import org.kybprototyping.notificationservice.domain.scheduled.servicetask.sendemail.SendEmailTaskContext

internal data class ServiceTaskOutputPublisherJob(
    private val transactionalExecutor: TransactionalExecutor,
    private val serviceTaskRepositoryPort: ServiceTaskRepositoryPort,
    private val serviceTaskPublisherPort: ServiceTaskPublisherPort,
) : ScheduledJob {
    override suspend fun execute() {
        logger.info("Task publisher job is being started...")

        var numOfFailures = 0
        var numOfSuccess = 0
        var shouldTerminate = false
        var i = 0
        while (i < NUM_OF_TASKS_TO_PUBLISH && !shouldTerminate) {
            transactionalExecutor.execute {
                serviceTaskRepositoryPort
                    .lockBy(publishableTypes, publishableStatuses)
                    .onLeft { failure -> logger.warn("No task could be locked: $failure") }
                    .onLeft { numOfFailures++ }
                    .onRight { taskToPublish ->
                        if (taskToPublish == null) { shouldTerminate = true }
                        else {
                            val output = deserialize(taskToPublish)
                            if (output == null) { numOfFailures++ }
                            else {
                                serviceTaskPublisherPort
                                    .execute(taskToPublish.type, taskToPublish.externalId, output)
                                    .onLeft { failure ->
                                        logger.warn("${taskToPublish.id} couldn't be published: $failure")
                                        numOfFailures++
                                    }
                                    .onRight {
                                        serviceTaskRepositoryPort
                                            .updateBy(taskToPublish.id, PUBLISHED)
                                            .onLeft {
                                                logger.warn("${taskToPublish.id} couldn't be set as PUBLISHED!!")
                                                numOfFailures++
                                            }
                                            .onRight { numOfSuccess++ }
                                    }
                            }
                        }
                    }
            }

            i++
        }

        logger.info("Task publisher job completed: numOfFailures: $numOfFailures, numOfSuccess: $numOfSuccess")
    }

    override val cron: String = "0/10 * * * * *"

    private companion object {
        private const val NUM_OF_TASKS_TO_PUBLISH = 10

        private val logger = cachedLoggerOf(ServiceTaskOutputPublisherJob::class.java)
        private val publishableTypes = listOf(SEND_EMAIL)
        private val publishableStatuses = listOf(COMPLETED, FAILED)
        private val objectMapper = jacksonObjectMapper()
            .registerModules(JavaTimeModule())
            .also { it.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) }// TODO: Use a common one!

        private fun deserialize(task: ServiceTask) =
            try {
                when(task.type) {
                    SEND_EMAIL -> {
                        objectMapper
                            .writeValueAsBytes(
                                objectMapper.treeToValue(task.context, SendEmailTaskContext::class.java).output!!
                            )
                    }
                }
            } catch (e: Exception) {
                logger.warn("Task serialization failed: ${task.id}", e)
                null
            }
    }
}
