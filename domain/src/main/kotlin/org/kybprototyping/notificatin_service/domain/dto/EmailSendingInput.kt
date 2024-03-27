package org.kybprototyping.notificatin_service.domain.dto

/**
 * Represents the input for email sending use case.
 */
data class EmailSendingInput(val type: EmailType, val language: EmailLanguage,
                             val to: String, val values: Map<String, String> = emptyMap())
