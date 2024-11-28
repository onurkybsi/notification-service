package org.kybprototyping.notificationservice.domain.usecase.notificationtemplate

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import org.apache.logging.log4j.kotlin.Logging
import org.kybprototyping.notificationservice.domain.common.DataConflictFailure
import org.kybprototyping.notificationservice.domain.common.Failure
import org.kybprototyping.notificationservice.domain.common.UnexpectedFailure
import org.kybprototyping.notificationservice.domain.common.UseCaseHandler
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort

internal class NotificationTemplateCreationUseCase(
    private val repositoryPort: NotificationTemplateRepositoryPort,
) : UseCaseHandler<NotificationTemplateCreationInput, Int>, Logging {
    override suspend fun handle(input: NotificationTemplateCreationInput): Either<Failure, Int> =
        repositoryPort.create(
            channel = input.channel,
            type = input.type,
            language = input.language,
            subject = input.subject,
            content = input.content,
        ).mapLeft {
            logger.error(it.cause) { "Something went unexpectedly wrong during creating a notification template: $input" }
            UnexpectedFailure(isTemporary = true)
        }.flatMap { createdTemplateId ->
            createdTemplateId?.right()
                ?: DataConflictFailure("Template with given ${input.channel}, ${input.type} and ${input.language} is already created!").left()
        }
}
