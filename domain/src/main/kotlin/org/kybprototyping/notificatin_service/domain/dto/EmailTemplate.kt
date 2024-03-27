package org.kybprototyping.notificatin_service.domain.dto

import java.time.OffsetDateTime

/**
 * Builder for [EmailTemplate] to create one.
 *
 * @param type type of email notification
 * @param language language of the template content
 * @param content email content with placeholders
 * @param modifiedBy latest modifier of the template
 * @param modificationDate latest modification date
 * @param createdBy creator of the template
 * @param creationDate creation date
 * @return built [EmailTemplate]
 */
fun forCreation(type: EmailType, language: EmailLanguage, content: String,
                modifiedBy: String? = null, modificationDate: OffsetDateTime,
                createdBy: String? = null, creationDate: OffsetDateTime): EmailTemplate {
    return EmailTemplate(null, type, language, content, modifiedBy, modificationDate, createdBy, creationDate)
}

/**
 * Represents the email templates.
 */
class EmailTemplate(val id: Long? = null, val type: EmailType, val language: EmailLanguage, var content: String,
                    var modifiedBy: String?, var modificationDate: OffsetDateTime, val createdBy: String?, val creationDate: OffsetDateTime) {
}

/**
 * Types of email notifications.
 */
enum class EmailType {

    WELCOME,
    PASSWORD_RESET

}

/**
 * Languages of email notifications.
 */
enum class EmailLanguage {

    EN,
    DE

}