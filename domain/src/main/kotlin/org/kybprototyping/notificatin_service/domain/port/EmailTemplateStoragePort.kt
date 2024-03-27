package org.kybprototyping.notificatin_service.domain.port

import org.kybprototyping.notificatin_service.domain.dto.EmailLanguage
import org.kybprototyping.notificatin_service.domain.dto.EmailTemplate
import org.kybprototyping.notificatin_service.domain.dto.EmailType
import org.kybprototyping.notificatin_service.domain.exception.UseCaseException

/**
 * Represents the API which provides the email template data stored in the underlying datasource.
 */
interface EmailTemplateStoragePort {

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