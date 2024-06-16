package org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.update

import org.kybprototyping.notificationservice.domain.model.NotificationTemplate
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort
import org.kybprototyping.notificationservice.domain.common.interfaces.InputOutputUseCaseHandler
import org.kybprototyping.notificationservice.domain.common.exception.nonExistentData
import org.kybprototyping.notificationservice.domain.common.interfaces.Transactional
import org.kybprototyping.notificationservice.domain.model.NotificationTemplateUpdateRequest

internal class NotificationTemplateUpdateUseCaseHandler(
    private val notificationTemplateRepositoryAdapter: NotificationTemplateRepositoryPort
): InputOutputUseCaseHandler<NotificationTemplateUpdateInput, NotificationTemplate?> {

    override suspend fun handle(input: NotificationTemplateUpdateInput): NotificationTemplate? {
        if (input.subject == null && input.content == null) {
            return null
        }
        return notificationTemplateRepositoryAdapter.updateBy(
            NotificationTemplateUpdateRequest(
                id = input.id,
                subject = input.subject,
                content = input.content
            )
        ) ?: throw nonExistentData("No notification template exists with given ID ${input.id}!")
    }

}