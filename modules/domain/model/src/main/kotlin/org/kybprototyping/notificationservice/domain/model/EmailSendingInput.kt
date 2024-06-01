package org.kybprototyping.notificationservice.domain.model

/**
 * Represents the input of email sending use case.
 */
data class EmailSendingInput(
    val type: NotificationType,
    val language: NotificationLanguage,
    val to: String,
    val values: Map<String, String> = emptyMap()
)
