package org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.update

import org.kybprototyping.notificationservice.domain.model.NotificationTemplate
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort
import org.kybprototyping.notificationservice.domain.usecase.InputOutputUseCaseHandler
import org.kybprototyping.notificationservice.domain.usecase.nonExistentData

internal class NotificationTemplateUpdateUseCaseHandler(
    private val notificationTemplateRepositoryAdapter: NotificationTemplateRepositoryPort
): InputOutputUseCaseHandler<NotificationTemplateUpdateInput, NotificationTemplate> {

    override suspend fun handle(input: NotificationTemplateUpdateInput): NotificationTemplate =
        notificationTemplateRepositoryAdapter.updateContent(input.id, input.content)
            ?: throw nonExistentData("No notification template exists with given ID ${input.id}!")

}