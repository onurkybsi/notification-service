package org.kybprototyping.notificationservice.adapter.rest.notificationtemplate

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Represents the notification template update parameters.")
internal data class NotificationTemplateUpdateRequest(
    @JsonProperty(required = false)
    @get:Schema(description = "Subject of the notification with placeholders if needed.", maxLength = 255)
    val subject: String?,
    @JsonProperty(required = false)
    @get:Schema(description = "Content of the notification with placeholders if needed.", maxLength = 30_000)
    val content: String?,
)
