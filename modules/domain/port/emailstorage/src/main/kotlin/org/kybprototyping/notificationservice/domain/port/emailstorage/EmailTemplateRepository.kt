package org.kybprototyping.notificationservice.domain.port.emailstorage

import org.kybprototyping.notificationservice.domain.common.exception.UseCaseException
import org.kybprototyping.notificationservice.domain.model.EmailLanguage
import org.kybprototyping.notificationservice.domain.model.EmailTemplate
import org.kybprototyping.notificationservice.domain.model.EmailType

/**
 * Represents the API which provides the email template data stored in the underlying datasource.
 */
interface EmailTemplateRepository {

    /**
     * Returns the email template with given [type] and [language].
     *
     * @param type type of email notification
     * @param language language of the template content
     * @return the email template with given [type] and [language]
     * @throws UseCaseException when no template exists with given [type] and [language]
     */
    fun get(type: EmailType, language: EmailLanguage): EmailTemplate

}
