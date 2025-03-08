package org.kybprototyping.notificationservice.adapter.rest.notification

import io.swagger.v3.oas.annotations.media.Schema
import org.kybprototyping.notificationservice.domain.model.NotificationLanguage
import org.kybprototyping.notificationservice.domain.model.NotificationType
import org.kybprototyping.notificationservice.domain.usecase.notification.SendEmailInput
import java.util.UUID

@Schema(description = "Send email task submission request.")
internal data class SendEmailRequest(
    val type: NotificationType,
    val language: NotificationLanguage,
    val to: String,
    val values: Map<String, String> = emptyMap()
) {
    internal companion object {
        internal fun SendEmailRequest.toEmailSendingInput(externalId: UUID? = null) =
            SendEmailInput(
                type = type,
                language = language,
                to = to,values = values,
                externalId = externalId
            )
    }
}
