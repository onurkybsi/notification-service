package org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.creation

import org.kybprototyping.notificationservice.domain.model.NotificationChannel
import org.kybprototyping.notificationservice.domain.model.NotificationLanguage
import org.kybprototyping.notificationservice.domain.model.NotificationTemplateCreationRequest
import org.kybprototyping.notificationservice.domain.model.NotificationType

/**
 * Represents the input of notification template creation use case.
 */
data class NotificationTemplateCreationInput(
    val channel: NotificationChannel,
    val type: NotificationType,
    val language: NotificationLanguage,
    val content: String,
)

/**
 * Builds [NotificationTemplateCreationRequest] from [this].
 *
 * @return built [NotificationTemplateCreationRequest]
 */
fun NotificationTemplateCreationInput.toNotificationTemplateCreationRequest() =
    NotificationTemplateCreationRequest(
        channel = channel,
        type = type,
        language = language,
        content = content
    )
