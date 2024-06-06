package org.kybprototyping.notificationservice.domain

import org.kybprototyping.notificationservice.common.Validator
import org.kybprototyping.notificationservice.domain.model.*
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort
import org.kybprototyping.notificationservice.domain.usecase.InputOnlyUseCaseHandler
import org.kybprototyping.notificationservice.domain.usecase.InputOutputUseCaseHandler
import org.kybprototyping.notificationservice.domain.usecase.emailsending.EmailSendingInput
import org.kybprototyping.notificationservice.domain.usecase.emailsending.EmailSendingUseCaseHandler
import org.kybprototyping.notificationservice.domain.usecase.emailsending.EmailSendingValidator
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplatecreation.NotificationTemplateCreationInput
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplatecreation.NotificationTemplateCreationOutput
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplatecreation.NotificationTemplateCreationUseCaseHandler
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplatesretrieval.NotificationTemplatesRetrievalInput
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplatesretrieval.NotificationTemplatesRetrievalUseCaseHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration("domain.usecase")
open class SpringConfiguration {

    @Bean
    internal open fun notificationTemplateCreationUseCaseHandler(
        notificationTemplateRepositoryPortAdapter: NotificationTemplateRepositoryPort
    ): InputOutputUseCaseHandler<NotificationTemplateCreationInput, NotificationTemplateCreationOutput> =
        NotificationTemplateCreationUseCaseHandler(notificationTemplateRepositoryPortAdapter)

    @Bean
    internal open fun notificationTemplatesRetrievalUseCaseHandler(
        notificationTemplateRepositoryPortAdapter: NotificationTemplateRepositoryPort
    ): InputOutputUseCaseHandler<NotificationTemplatesRetrievalInput, List<NotificationTemplate>> =
        NotificationTemplatesRetrievalUseCaseHandler(notificationTemplateRepositoryPortAdapter)

    @Bean
    internal open fun emailSendingUseCaseHandler(
        validator: Validator<EmailSendingInput>,
        notificationTemplateRepositoryPortAdapter: NotificationTemplateRepositoryPort
    ): InputOnlyUseCaseHandler<EmailSendingInput> {
        return EmailSendingUseCaseHandler(EmailSendingValidator(), notificationTemplateRepositoryPortAdapter)
    }

}