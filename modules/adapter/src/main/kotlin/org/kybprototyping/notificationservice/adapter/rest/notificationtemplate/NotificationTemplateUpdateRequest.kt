package org.kybprototyping.notificationservice.adapter.rest.notificationtemplate

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max

@Schema(description = "Represents the notification template update parameters.")
internal data class NotificationTemplateUpdateRequest(
    @JsonProperty(required = false)
    @Max(255)
    val subject: String?,
    @JsonProperty(required = false)
    @Max(30_000)
    val content: String?,
)
