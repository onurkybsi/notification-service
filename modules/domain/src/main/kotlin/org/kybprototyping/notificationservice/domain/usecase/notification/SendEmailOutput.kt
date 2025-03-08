package org.kybprototyping.notificationservice.domain.usecase.notification

import java.util.UUID

/**
 * Represents the output of send email use case.
 */
data class SendEmailOutput(val externalId: UUID)
