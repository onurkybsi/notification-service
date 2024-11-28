package org.kybprototyping.notificationservice.domain.usecase.notificationtemplate

import org.kybprototyping.notificationservice.domain.model.NotificationChannel
import org.kybprototyping.notificationservice.domain.model.NotificationLanguage
import org.kybprototyping.notificationservice.domain.model.NotificationType

/**
 * Represents the input parameters of notification template creation use case.
 */
data class NotificationTemplateCreationInput(
    val channel: NotificationChannel,
    val type: NotificationType,
    val language: NotificationLanguage,
    val subject: String,
    val content: String,
)
