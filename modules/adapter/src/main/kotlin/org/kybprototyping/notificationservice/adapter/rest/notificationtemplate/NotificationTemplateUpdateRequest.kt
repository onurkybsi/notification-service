package org.kybprototyping.notificationservice.adapter.rest.notificationtemplate

internal data class NotificationTemplateUpdateRequest(
    val subject: String?,
    val content: String?
)