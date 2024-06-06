package org.kybprototyping.notificationservice.domain.model

/**
 * Represents the required parameters for [NotificationTemplate] creation.
 */
data class NotificationTemplateCreationRequest(
    val channel: NotificationChannel,
    val type: NotificationType,
    val language: NotificationLanguage,
    val content: String,
)