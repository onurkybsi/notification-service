package org.kybprototyping.notificationservice.domain.usecase.notification

import org.kybprototyping.notificationservice.domain.model.NotificationLanguage
import org.kybprototyping.notificationservice.domain.model.NotificationType
import java.util.UUID

/**
 * Represents the input of send email use case.
 */
data class SendEmailInput(
    val type: NotificationType,
    val language: NotificationLanguage,
    val to: String,
    val values: Map<String, String>,
    val externalId: UUID?,
)
