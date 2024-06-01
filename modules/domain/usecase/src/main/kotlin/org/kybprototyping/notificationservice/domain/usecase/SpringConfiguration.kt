package org.kybprototyping.notificationservice.domain.usecase

import org.kybprototyping.notificationservice.common.Validator
import org.kybprototyping.notificationservice.domain.model.EmailSendingInput
import org.kybprototyping.notificationservice.domain.port.notificationtemplaterepository.NotificationTemplateRepository
import org.kybprototyping.notificationservice.domain.usecase.emailsending.EmailSendingUseCaseHandler
import org.kybprototyping.notificationservice.domain.usecase.emailsending.EmailSendingValidator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class SpringConfiguration {

    @Bean
    open fun emailSendingUseCaseHandler(
        validator: Validator<EmailSendingInput>,
        notificationTemplateRepositoryAdapter: NotificationTemplateRepository
    ): InputOnlyUseCaseHandler<EmailSendingInput> {
        return EmailSendingUseCaseHandler(validator, notificationTemplateRepositoryAdapter)
    }

    @Bean
    open fun emailSendingValidator(): Validator<EmailSendingInput> {
        return EmailSendingValidator()
    }

}