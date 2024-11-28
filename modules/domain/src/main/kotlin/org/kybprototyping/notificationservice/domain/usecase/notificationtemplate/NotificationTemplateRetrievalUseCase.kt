package org.kybprototyping.notificationservice.domain.usecase.notificationtemplate

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import org.apache.logging.log4j.kotlin.Logging
import org.kybprototyping.notificationservice.domain.common.DataNotFoundFailure
import org.kybprototyping.notificationservice.domain.common.Failure
import org.kybprototyping.notificationservice.domain.common.UnexpectedFailure
import org.kybprototyping.notificationservice.domain.common.UseCaseHandler
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort

internal class NotificationTemplateRetrievalUseCase(
    private val repositoryPort: NotificationTemplateRepositoryPort,
) : UseCaseHandler<Int, NotificationTemplate?>, Logging {
    override suspend fun handle(input: Int): Either<Failure, NotificationTemplate> =
        repositoryPort.findById(input)
            .mapLeft {
                logger.error(it.cause) { "Something went unexpectedly wrong during fetching notification template with ID: $input" }
                UnexpectedFailure(isTemporary = true)
            }
            .flatMap {
                it?.right() ?: DataNotFoundFailure("No notification template found by given ID: $input").left()
            }
}
