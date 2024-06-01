package org.kybprototyping.notificationservice.domain.model

/**
 * Represents the input of notification template creation use case.
 */
data class NotificationTemplateCreationInput(
    val channel: NotificationChannel,
    val type: NotificationType,
    val language: NotificationLanguage,
    val content: String,
)