package org.kybprototyping.notificationservice.domain

import org.kybprototyping.notificationservice.domain.model.*
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort
import org.kybprototyping.notificationservice.domain.usecase.InputOnlyUseCaseHandler
import org.kybprototyping.notificationservice.domain.usecase.InputOutputUseCaseHandler
import org.kybprototyping.notificationservice.domain.usecase.notification.sending.EmailSendingInput
import org.kybprototyping.notificationservice.domain.usecase.notification.sending.EmailSendingUseCaseHandler
import org.kybprototyping.notificationservice.domain.usecase.notification.sending.EmailSendingValidator
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.creation.NotificationTemplateCreationInput
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.creation.NotificationTemplateCreationOutput
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.creation.NotificationTemplateCreationUseCaseHandler
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.retrieval.NotificationTemplatesRetrievalInput
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.retrieval.NotificationTemplatesRetrievalUseCaseHandler
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
        notificationTemplateRepositoryPortAdapter: NotificationTemplateRepositoryPort
    ): InputOnlyUseCaseHandler<EmailSendingInput> {
        return EmailSendingUseCaseHandler(EmailSendingValidator(), notificationTemplateRepositoryPortAdapter)
    }

}