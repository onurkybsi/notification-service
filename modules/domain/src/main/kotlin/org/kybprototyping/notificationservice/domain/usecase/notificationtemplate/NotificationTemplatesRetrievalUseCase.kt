package org.kybprototyping.notificationservice.domain.usecase.notificationtemplate

import org.apache.logging.log4j.kotlin.Logging
import org.kybprototyping.notificationservice.domain.common.UnexpectedFailure
import org.kybprototyping.notificationservice.domain.common.UseCaseHandler
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort

internal class NotificationTemplatesRetrievalUseCase(
    private val repositoryPort: NotificationTemplateRepositoryPort
) : UseCaseHandler<NotificationTemplatesRetrievalInput, List<NotificationTemplate>>, Logging {
    override suspend fun handle(input: NotificationTemplatesRetrievalInput) =
        repositoryPort.findBy(input.channel, input.type, input.language)
            .mapLeft {
                logger.error(it.cause) { "Something went unexpectedly wrong during fetching notification templates: $input" }
                UnexpectedFailure(isTemporary = true)
            }
}