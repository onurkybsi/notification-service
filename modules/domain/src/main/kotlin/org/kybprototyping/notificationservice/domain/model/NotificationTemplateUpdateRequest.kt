package org.kybprototyping.notificationservice.domain.model

/**
 * Represent the values which can be updated on a [NotificationTemplate].
 */
data class NotificationTemplateUpdateRequest(
    val id: Int,
    val subject: String?,
    val content: String?
)
