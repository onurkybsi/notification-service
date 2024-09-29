package org.kybprototyping.notificationservice.domain.usecase.notificationtemplate

import arrow.core.Either
import org.apache.logging.log4j.kotlin.Logging
import org.kybprototyping.notificationservice.domain.common.DataNotFoundFailure
import org.kybprototyping.notificationservice.domain.common.Failure
import org.kybprototyping.notificationservice.domain.common.UnexpectedFailure
import org.kybprototyping.notificationservice.domain.common.UseCaseHandler
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort

internal class NotificationTemplateDeletionUseCase(
    private val repositoryPort: NotificationTemplateRepositoryPort
) : UseCaseHandler<Int, Unit>, Logging {
    override suspend fun handle(input: Int): Either<Failure, Unit> =
        repositoryPort.delete(input).mapLeft { toFailure(it, input) }

    private fun toFailure(from: NotificationTemplateRepositoryPort.DeletionFailure, id: Int): Failure =
        when(from) {
            is NotificationTemplateRepositoryPort.DeletionFailure.DataNotFoundFailure -> {
                DataNotFoundFailure(message = "No notification template found to delete by given ID: $id")
            }
            is NotificationTemplateRepositoryPort.DeletionFailure.UnexpectedFailure -> {
                logger.error(from.cause) { "Something went unexpectedly wrong during deletion of a notification template: $id" }
                UnexpectedFailure(isTemporary = true)
            }
        }

}