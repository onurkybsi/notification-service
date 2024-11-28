package org.kybprototyping.notificationservice.adapter.rest.notificationtemplate

import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "Represents the notification templates.")
internal data class NotificationTemplate(
    val id: Int,
    val channel: NotificationChannel,
    val type: NotificationType,
    val language: NotificationLanguage,
    @get:Schema(description = "Subject of the notification with placeholders if needed.")
    val subject: String,
    @get:Schema(description = "Content of the notification with placeholders if needed.")
    val content: String,
    val modifiedBy: String? = null,
    val modifiedAt: OffsetDateTime,
    val createdBy: String? = null,
    val createdAt: OffsetDateTime,
)
