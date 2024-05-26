package org.kybprototyping.notificationservice.domain.model

/**
 * Represents the input for email sending use case.
 */
data class EmailSendingInput(
    val type: EmailType,
    val language: EmailLanguage,
    val to: String,
    val values: Map<String, String> = emptyMap()
)
