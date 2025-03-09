package org.kybprototyping.notificationservice.domain.scheduled.servicetask.sendemail

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.right
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.commons.text.StringSubstitutor
import org.apache.logging.log4j.kotlin.cachedLoggerOf
import org.kybprototying.notificationservice.common.Failure as DomainFailure
import org.kybprototying.notificationservice.common.TimeUtils
import org.kybprototying.notificationservice.common.runExceptionCatching
import org.kybprototyping.notificationservice.domain.model.NotificationChannel.EMAIL
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate
import org.kybprototyping.notificationservice.domain.model.ServiceTask
import org.kybprototyping.notificationservice.domain.model.ServiceTaskStatus
import org.kybprototyping.notificationservice.domain.port.EmailSenderPort
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort
import org.kybprototyping.notificationservice.domain.port.ServiceTaskRepositoryPort
import org.kybprototyping.notificationservice.domain.scheduled.servicetask.ServiceTaskExecutor
import org.kybprototyping.notificationservice.domain.scheduled.servicetask.sendemail.SendEmailTaskContext
import org.kybprototyping.notificationservice.domain.scheduled.servicetask.sendemail.SendEmailTaskContext as Context
import org.kybprototyping.notificationservice.domain.scheduled.servicetask.sendemail.SendEmailTaskContext.Companion.with
import org.kybprototyping.notificationservice.domain.scheduled.servicetask.sendemail.SendEmailTaskContext.Failure
import org.kybprototyping.notificationservice.domain.scheduled.servicetask.sendemail.SendEmailTaskContext.FailureType
import org.kybprototyping.notificationservice.domain.scheduled.servicetask.sendemail.SendEmailTaskContext.FailureType.TEMPLATE_NOT_FOUND
import org.kybprototyping.notificationservice.domain.scheduled.servicetask.sendemail.SendEmailTaskContext.FailureType.EMAIL_SENDER_FAILURE
import org.kybprototyping.notificationservice.domain.scheduled.servicetask.sendemail.SendEmailTaskContext.Output
import java.time.OffsetDateTime

internal class SendEmailTaskExecutor(
    private val properties: Properties,
    private val templateRepositoryPort: NotificationTemplateRepositoryPort,
    private val serviceTaskRepositoryPort: ServiceTaskRepositoryPort,
    private val emailSenderPort: EmailSenderPort,
    private val timeUtils: TimeUtils,
) : ServiceTaskExecutor {
    override suspend fun execute(task: ServiceTask): Either<DomainFailure, Unit> =
        extractContext(task)
            .flatMap { ctx ->
                getTemplate(task, ctx)
                    .flatMap { template -> getEnrichedTemplate(ctx, template) }
                    .flatMap { (enrichedSubject, enrichedContent) -> sendEmail(task, ctx, enrichedSubject, enrichedContent) }
                    .flatMap { setAsCompleted(task, ctx) }
            }

    private fun extractContext(from: ServiceTask) =
        runExceptionCatching {
            objectMapper.treeToValue(from.context, SendEmailTaskContext::class.java).right()
        }
            .onLeft { logger.warn("Input couldn't be deserialized from $from, failure: $it") }

    private suspend fun getTemplate(task: ServiceTask, ctx: Context) =
        templateRepositoryPort
            .findOneBy(EMAIL, ctx.input.type, ctx.input.language)
            .onLeft {
                setAsError(
                    task = task,
                    ctx = ctx,
                    executionScheduledAtToSet = timeUtils
                        .nowAsOffsetDateTime()
                        .plusHours(properties.templateNotFoundBackoffHour.toLong().times(task.executionCount + 1)),
                    failureTypeToSet = TEMPLATE_NOT_FOUND,
                )
            }

    // TODO: Does this throw exception?
    private fun getEnrichedTemplate(ctx: Context, template: NotificationTemplate) =
        StringSubstitutor(ctx.input.values)
            .let { substitutor ->
                (substitutor.replace(template.subject) to substitutor.replace(template.content)).right()
            }

    private suspend fun sendEmail(task: ServiceTask, ctx: Context, enrichedSubject: String, enrichedContent: String) =
        emailSenderPort
            .send(
                from = properties.senderAddress,
                to = ctx.input.to,
                subject = enrichedSubject,
                content = enrichedContent,
            )
            .onLeft {
                setAsError(
                    task = task,
                    ctx = ctx,
                    executionScheduledAtToSet = timeUtils
                        .nowAsOffsetDateTime()
                        .plusMinutes(
                            properties
                                .emailSenderFailureBackoffMin
                                .toLong()
                                .times(task.executionCount + 1),
                        ),
                    failureTypeToSet = EMAIL_SENDER_FAILURE,
                )
            }

    private suspend fun setAsCompleted(task: ServiceTask, ctx: Context) =
        serviceTaskRepositoryPort
            .updateBy(
                id = task.id,
                statusToSet = ServiceTaskStatus.COMPLETED,
                executionCountToSet = task.executionCount + 1,
                executionStartedAtToSet = null,
                executionScheduledAtToSet = null,
                contextToSet = objectMapper.valueToTree(ctx.copy(output = Output(task.externalId, Context.Status.SUCCESSFUL))),
                messageToSet = null,
            )
            .onLeft { logger.warn("Update to COMPLETED failed: $task") }

    private suspend fun setAsError(
        task: ServiceTask,
        ctx: Context,
        executionScheduledAtToSet: OffsetDateTime,
        failureTypeToSet: FailureType,
    ) =
        if (task.executionCount + 1 == properties.maxExecutionCount) {
            serviceTaskRepositoryPort
                .updateBy(
                    id = task.id,
                    statusToSet = ServiceTaskStatus.FAILED,
                    executionCountToSet = task.executionCount + 1,
                    executionStartedAtToSet = null,
                    executionScheduledAtToSet = null,
                    contextToSet = objectMapper.valueToTree(
                        ctx
                            .with(Failure(timeUtils.nowAsOffsetDateTime(), failureTypeToSet))
                            .with(Output(task.externalId, Context.Status.FAILED, ctx.failures?.first()?.type ?: failureTypeToSet))
                    ),
                    messageToSet = "Max execution count ${properties.maxExecutionCount} reached!",
                )
                .onLeft { logger.warn("Update to FAILED failed: $task") }
        } else {
            serviceTaskRepositoryPort
                .updateBy(
                    id = task.id,
                    statusToSet = ServiceTaskStatus.ERROR,
                    executionCountToSet = task.executionCount + 1,
                    executionStartedAtToSet = null,
                    executionScheduledAtToSet = executionScheduledAtToSet,
                    contextToSet = objectMapper.valueToTree(ctx.with(Failure(timeUtils.nowAsOffsetDateTime(), failureTypeToSet))),
                    messageToSet = null,
                )
                .onLeft { logger.warn("Update to ERROR failed: $task") }
        }

    private companion object {
        private val logger = cachedLoggerOf(SendEmailTaskExecutor::class.java)
        private val objectMapper = jacksonObjectMapper()
            .registerModules(JavaTimeModule())
            .also { it.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) }// TODO: Use a common one!
    }

    internal data class Properties(
        val senderAddress: String,
        val maxExecutionCount: Int,
        val templateNotFoundBackoffHour: Int,
        val emailSenderFailureBackoffMin: Int,
    )
}
