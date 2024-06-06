package org.kybprototyping.notificationservice.domain.usecase.notificationtemplatesretrieval

import org.kybprototyping.notificationservice.domain.model.NotificationChannel
import org.kybprototyping.notificationservice.domain.model.NotificationLanguage
import org.kybprototyping.notificationservice.domain.model.NotificationType

/**
 * Represents the input of notification templates retrieval use case.
 */
data class NotificationTemplatesRetrievalInput(
    val channel: NotificationChannel? = null,
    val type: NotificationType? = null,
    val language: NotificationLanguage? = null,
)