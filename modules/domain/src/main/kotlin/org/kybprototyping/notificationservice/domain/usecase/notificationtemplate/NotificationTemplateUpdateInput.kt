package org.kybprototyping.notificationservice.domain.usecase.notificationtemplate

/**
 * Represents the input parameters of notification template update use case.
 *
 * @param id template ID to update
 * @param subject subject value to set, or **null** to ignore to update subject
 * @param content content value to set, or **null** to ignore to update content
 */
data class NotificationTemplateUpdateInput(
    val id: Int,
    val subject: String?,
    val content: String?,
)