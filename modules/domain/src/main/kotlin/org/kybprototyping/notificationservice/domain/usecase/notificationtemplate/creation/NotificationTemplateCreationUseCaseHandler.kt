package org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.creation

import org.kybprototyping.notificationservice.domain.model.NotificationTemplateCreationRequest
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort
import org.kybprototyping.notificationservice.domain.common.interfaces.Transactional
import org.kybprototyping.notificationservice.domain.common.interfaces.InputOutputUseCaseHandler

internal open class NotificationTemplateCreationUseCaseHandler(
    private val notificationTemplateRepositoryPortAdapter: NotificationTemplateRepositoryPort
) : InputOutputUseCaseHandler<NotificationTemplateCreationInput, NotificationTemplateCreationOutput> {

    @Transactional
    override suspend fun handle(input: NotificationTemplateCreationInput): NotificationTemplateCreationOutput =
        NotificationTemplateCreationOutput(
            notificationTemplateRepositoryPortAdapter.create(toNotificationTemplateCreationRequest(input))
        )

}

private fun toNotificationTemplateCreationRequest(input: NotificationTemplateCreationInput) =
    NotificationTemplateCreationRequest(
        channel = input.channel,
        type = input.type,
        language = input.language,
        content = input.content
    )
