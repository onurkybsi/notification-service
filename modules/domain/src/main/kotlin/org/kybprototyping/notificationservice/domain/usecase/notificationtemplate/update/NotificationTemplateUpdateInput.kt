package org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.update

/**
 * Represents the input of notification template update use case.
 */
data class NotificationTemplateUpdateInput(
    val id: Int,
    val content: String
)
