package org.kybprototyping.notificationservice.adapter.rest.notification

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Send email task submission response.")
data class SendEmailResponse(
    @get:Schema(description = """
        Send email task ID. Eventually, when the task is completed successfully or unsuccessfully, the result is published with this ID.
        So, the client can make use of this ID to link the email sent with their own processes.
        This ID can be set through 'X-Idempotency-Key' header as part of the request.
    """) val id: UUID
)
