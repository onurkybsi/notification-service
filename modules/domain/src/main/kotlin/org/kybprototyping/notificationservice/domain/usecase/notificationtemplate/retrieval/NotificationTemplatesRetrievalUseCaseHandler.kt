package org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.retrieval

import org.kybprototyping.notificationservice.domain.model.NotificationTemplate
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort
import org.kybprototyping.notificationservice.domain.common.interfaces.InputOutputUseCaseHandler

internal class NotificationTemplatesRetrievalUseCaseHandler(
    private val notificationTemplateRepositoryPort: NotificationTemplateRepositoryPort
): InputOutputUseCaseHandler<NotificationTemplatesRetrievalInput, List<NotificationTemplate>> {

    override suspend fun handle(input: NotificationTemplatesRetrievalInput): List<NotificationTemplate> =
        notificationTemplateRepositoryPort.getListBy(input.channel, input.type, input.language)

}