package org.kybprototyping.notificationservice.domain.usecase.notificationtemplate

import arrow.core.Either
import arrow.core.right
import org.apache.logging.log4j.kotlin.Logging
import org.apache.logging.log4j.kotlin.logger
import org.kybprototyping.notificationservice.domain.common.DataNotFoundFailure
import org.kybprototyping.notificationservice.domain.common.Failure
import org.kybprototyping.notificationservice.domain.common.UnexpectedFailure
import org.kybprototyping.notificationservice.domain.common.UseCaseHandler
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort

internal class NotificationTemplateUpdateUseCase(
    private val repositoryPort: NotificationTemplateRepositoryPort
) : UseCaseHandler<NotificationTemplateUpdateInput, Unit>, Logging {
    override suspend fun handle(input: NotificationTemplateUpdateInput): Either<Failure, Unit> {
        if (input.subject == null && input.content == null) {
            return Unit.right()
        }
        return repositoryPort.update(input.id, input.subject, input.content).mapLeft { toFailure(it, input) }
    }

    private companion object {
        private fun toFailure(
            from: NotificationTemplateRepositoryPort.UpdateFailure,
            input: NotificationTemplateUpdateInput
        ): Failure =
            when(from) {
                is NotificationTemplateRepositoryPort.UpdateFailure.DataNotFoundFailure -> {
                    DataNotFoundFailure(message = "No notification template found to update by given ID: ${input.id}")
                }
                is NotificationTemplateRepositoryPort.UpdateFailure.UnexpectedFailure -> {
                    logger.error(from.cause) { "Something went unexpectedly wrong during update of a notification template: $input" }
                    UnexpectedFailure(isTemporary = true)
                }
            }
    }
}