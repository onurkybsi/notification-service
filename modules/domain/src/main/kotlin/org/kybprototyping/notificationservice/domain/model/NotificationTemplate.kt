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
    val subject: String,
    val content: String,
    val modifiedBy: String? = null,
    val modifiedAt: OffsetDateTime,
    val createdBy: String? = null,
    val createdAt: OffsetDateTime
)