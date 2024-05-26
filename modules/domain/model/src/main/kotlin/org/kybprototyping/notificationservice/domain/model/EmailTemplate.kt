package org.kybprototyping.notificationservice.domain.model

import java.time.OffsetDateTime

/**
 * Represents the email templates.
 */
data class EmailTemplate(
    val id: Long,
    val type: EmailType,
    val language: EmailLanguage,
    var content: String,
    var modifiedBy: String?,
    var modificationDate: OffsetDateTime,
    val createdBy: String?,
    val creationDate: OffsetDateTime
)
