package org.kybprototyping.notificationservice.domain.model

import java.time.OffsetDateTime

/**
 * Represents the notification templates.
 */
data class NotificationTemplate(
    val id: Int,
    val channel: NotificationChannel,
    val type: NotificationType,
    val language: NotificationLanguage,
    val content: String,
    val modifiedBy: String?,
    val modificationDate: OffsetDateTime,
    val createdBy: String?,
    val creationDate: OffsetDateTime
)
