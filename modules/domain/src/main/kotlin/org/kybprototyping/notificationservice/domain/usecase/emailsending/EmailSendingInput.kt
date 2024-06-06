package org.kybprototyping.notificationservice.domain.usecase.emailsending

import org.kybprototyping.notificationservice.domain.model.NotificationLanguage
import org.kybprototyping.notificationservice.domain.model.NotificationType

/**
 * Represents the input of email sending use case.
 */
data class EmailSendingInput(
    val type: NotificationType,
    val language: NotificationLanguage,
    val to: String,
    val values: Map<String, String> = emptyMap()
)
