package org.kybprototyping.notificationservice.domain.usecase.notificationtemplatecreation

import org.kybprototyping.notificationservice.domain.model.NotificationTemplateCreationInput
import org.kybprototyping.notificationservice.domain.model.NotificationTemplateCreationOutput
import org.kybprototyping.notificationservice.domain.model.NotificationTemplateCreationRequest
import org.kybprototyping.notificationservice.domain.port.notificationtemplaterepository.NotificationTemplateRepository
import org.kybprototyping.notificationservice.domain.usecase.InputOutputUseCaseHandler

internal class NotificationTemplateCreationUseCaseHandler(
    private val notificationTemplateRepositoryAdapter: NotificationTemplateRepository
) :
    InputOutputUseCaseHandler<NotificationTemplateCreationInput, NotificationTemplateCreationOutput> {
    override suspend fun handle(input: NotificationTemplateCreationInput): NotificationTemplateCreationOutput =
        NotificationTemplateCreationOutput(
            notificationTemplateRepositoryAdapter.create(toNotificationTemplateCreationRequest(input))
        )

}

private fun toNotificationTemplateCreationRequest(input: NotificationTemplateCreationInput) =
    NotificationTemplateCreationRequest(
        channel = input.channel,
        type = input.type,
        language = input.language,
        content = input.content
    )