package org.kybprototyping.notificationservice.adapter.rest.notificationtemplate

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import org.kybprototyping.notificationservice.adapter.rest.notificationtemplate.NotificationChannel.Companion.toDomain
import org.kybprototyping.notificationservice.adapter.rest.notificationtemplate.NotificationLanguage.Companion.toDomain
import org.kybprototyping.notificationservice.adapter.rest.notificationtemplate.NotificationType.Companion.toDomain
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplateCreationInput

@Schema(description = "Represents the notification template creation parameters.")
internal data class NotificationTemplateCreationRequest(
    val channel: NotificationChannel,
    val type: NotificationType,
    val language: NotificationLanguage,
    @Max(255)
    @get:Schema(description = "Subject of the notification with placeholders if needed.")
    val subject: String,
    @Max(30_000)
    @get:Schema(description = "Content of the notification with placeholders if needed.")
    val content: String,
) {
    internal companion object {
        internal fun NotificationTemplateCreationRequest.toDomain() =
            NotificationTemplateCreationInput(
                channel = channel.toDomain(),
                type = type.toDomain(),
                language = language.toDomain(),
                subject = subject,
                content = content,
            )
    }
}
