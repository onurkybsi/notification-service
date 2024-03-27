package org.kybprototyping.notificatin_service.domain

import org.kybprototyping.notificatin_service.domain.dto.EmailSendingInput
import org.kybprototyping.notificatin_service.domain.interfaces.InputOnlyUseCaseHandler
import org.kybprototyping.notificatin_service.domain.interfaces.Validator
import org.kybprototyping.notificatin_service.domain.port.EmailTemplateStoragePort
import org.kybprototyping.notificatin_service.domain.validator.EmailSendingValidator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class SpringConfiguration {

    @Bean
    open fun emailSendingUseCaseHandler(validator: Validator<EmailSendingInput>,
                                        emailTemplateStorageAdapter: EmailTemplateStoragePort): InputOnlyUseCaseHandler<EmailSendingInput> {
        return EmailSendingUseCaseHandler(validator, emailTemplateStorageAdapter)
    }

    @Bean
    open fun emailSendingValidator(): Validator<EmailSendingInput> {
        return EmailSendingValidator()
    }

}