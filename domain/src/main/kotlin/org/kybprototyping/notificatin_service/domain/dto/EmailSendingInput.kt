package org.kybprototyping.notificatin_service.domain.dto

/**
 * Represents the input for email sending use case.
 */
data class EmailSendingInput(val to: String, val values: Map<String, String> = emptyMap())
