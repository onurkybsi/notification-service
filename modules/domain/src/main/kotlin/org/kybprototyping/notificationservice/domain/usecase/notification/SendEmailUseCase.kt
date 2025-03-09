package org.kybprototyping.notificationservice.domain.usecase.notification

import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.logging.log4j.kotlin.logger
import org.kybprototying.notificationservice.common.*
import org.kybprototying.notificationservice.common.UnexpectedFailure.Companion.unexpectedFailure
import org.kybprototyping.notificationservice.domain.common.UseCaseHandler
import org.kybprototyping.notificationservice.domain.model.ServiceTask
import org.kybprototyping.notificationservice.domain.model.ServiceTaskPriority
import org.kybprototyping.notificationservice.domain.model.ServiceTaskStatus
import org.kybprototyping.notificationservice.domain.model.ServiceTaskType
import org.kybprototyping.notificationservice.domain.port.ServiceTaskRepositoryPort
import org.kybprototyping.notificationservice.domain.scheduled.servicetask.sendemail.SendEmailTaskContext
import java.util.UUID
import java.util.regex.Pattern

internal class SendEmailUseCase(
    private val timeUtils: TimeUtils,
    private val serviceTaskRepositoryPort: ServiceTaskRepositoryPort,
) : UseCaseHandler<SendEmailInput, SendEmailOutput> {
    override suspend fun handle(input: SendEmailInput) =
        validate(input)
            .flatMap { toServiceTask(input).right() }
            .flatMap {
                taskToInsert -> serviceTaskRepositoryPort.insert(taskToInsert)
                    .mapLeft { failure ->
                        when(failure) {
                            is DataConflictFailure -> {
                                DataConflictFailure("There is already a send email task created with given external ID: ${input.externalId}")
                            }
                            is DataInvalidityFailure, is DataNotFoundFailure, is UnexpectedFailure -> {
                                logger.warn("Unexpected failure occurred: $failure, input: $input")
                                return@mapLeft unexpectedFailure
                            }
                        }
                    }
                    .map { SendEmailOutput(taskToInsert.externalId) }
            }

    private fun validate(input: SendEmailInput) =
        if (!regexEmail.matcher(input.to).matches()) {
            DataInvalidityFailure(
                message = "Give input is not valid!",
                validationResult = ValidationResult.from(
                    "to" to arrayOf("must be an email address"),
                ),
            ).left()
        } else {
            Unit.right()
        }

    private fun toServiceTask(from: SendEmailInput): ServiceTask {
        val now = timeUtils.nowAsOffsetDateTime()
        return ServiceTask(
            id = UUID.randomUUID(), // TODO: UUIDGenerator!
            type = ServiceTaskType.SEND_EMAIL,
            status = ServiceTaskStatus.PENDING,
            externalId = from.externalId ?: UUID.randomUUID(), // TODO: UUIDGenerator!
            priority = ServiceTaskPriority.MEDIUM,
            executionCount = 0,
            executionStartedAt = null,
            executionScheduledAt = null,
            context = objectMapper.valueToTree(SendEmailTaskContext(from)),
            message = null,
            modifiedAt = now,
            createdAt = now,
        )
    }

    private companion object {
        private val regexEmail = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$")
        private val objectMapper = jacksonObjectMapper() // TODO: Change common one!
    }
}
