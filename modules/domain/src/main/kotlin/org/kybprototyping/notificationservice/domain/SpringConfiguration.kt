package org.kybprototyping.notificationservice.domain

import org.kybprototying.notificationservice.common.TimeUtils
import org.kybprototyping.notificationservice.domain.common.CoroutineDispatcherProvider
import org.kybprototyping.notificationservice.domain.common.UseCaseHandler
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate
import org.kybprototyping.notificationservice.domain.model.ServiceTaskType
import org.kybprototyping.notificationservice.domain.port.EmailSenderPort
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort
import org.kybprototyping.notificationservice.domain.port.ServiceTaskRepositoryPort
import org.kybprototyping.notificationservice.domain.scheduled.servicetask.ServiceTaskExecutorJob
import org.kybprototyping.notificationservice.domain.scheduled.servicetask.sendemail.SendEmailTaskExecutor
import org.kybprototyping.notificationservice.domain.usecase.notification.SendEmailUseCase
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplateCreationUseCase
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplateDeletionUseCase
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplateRetrievalUseCase
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplateUpdateUseCase
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplatesRetrievalInput
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplatesRetrievalUseCase
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration("domain")
internal open class SpringConfiguration {
    @Bean
    internal open fun notificationTemplatesRetrievalUseCase(
        repositoryPort: NotificationTemplateRepositoryPort,
    ): UseCaseHandler<NotificationTemplatesRetrievalInput, List<NotificationTemplate>> = NotificationTemplatesRetrievalUseCase(repositoryPort)

    @Bean
    internal open fun notificationTemplateRetrievalUseCase(
        repositoryPort: NotificationTemplateRepositoryPort,
    ): UseCaseHandler<Int, NotificationTemplate?> = NotificationTemplateRetrievalUseCase(repositoryPort)

    @Bean
    internal open fun notificationTemplateCreationUseCase(repositoryPort: NotificationTemplateRepositoryPort) =
        NotificationTemplateCreationUseCase(repositoryPort)

    @Bean
    internal open fun notificationTemplateDeletionUseCase(repositoryPort: NotificationTemplateRepositoryPort) =
        NotificationTemplateDeletionUseCase(repositoryPort)

    @Bean
    internal open fun notificationTemplateUpdateUseCase(repositoryPort: NotificationTemplateRepositoryPort) =
        NotificationTemplateUpdateUseCase(repositoryPort)

    @Bean
    internal open fun serviceTaskExecutorJob(
        coroutineDispatcherProvider: CoroutineDispatcherProvider,
        timeUtils: TimeUtils,
        serviceTaskRepositoryPort: ServiceTaskRepositoryPort,
        sendEmailTaskExecutor: SendEmailTaskExecutor,
    ) = ServiceTaskExecutorJob(
        coroutineDispatcherProvider,
        timeUtils,
        serviceTaskRepositoryPort,
        mapOf(ServiceTaskType.SEND_EMAIL to sendEmailTaskExecutor),
    )

    @Bean
    internal open fun sendEmailUseCase(
        timeUtils: TimeUtils,
        serviceTaskRepositoryPort: ServiceTaskRepositoryPort,
    ) = SendEmailUseCase(timeUtils, serviceTaskRepositoryPort)

    @Bean
    internal open fun sendEmailTaskExecutorProperties(
        @Value("\${scheduled.service-task.send-email.sender-address}") senderAddress: String,
        @Value("\${scheduled.service-task.send-email.max-execution-count}") maxExecutionCount: Int,
        @Value("\${scheduled.service-task.send-email.template-not-found-backoff-hour}") templateNotFoundBackoffHour: Int,
        @Value("\${scheduled.service-task.send-email.email-sender-failure-backoff-min}") emailSenderFailureBackoffMin: Int,
    ) =
        SendEmailTaskExecutor.Properties(
            senderAddress,
            maxExecutionCount,
            templateNotFoundBackoffHour,
            emailSenderFailureBackoffMin,
        )

    @Bean
    internal open fun sendEmailTaskExecutor(
        properties: SendEmailTaskExecutor.Properties,
        templateRepositoryPort: NotificationTemplateRepositoryPort,
        serviceTaskRepositoryPort: ServiceTaskRepositoryPort,
        emailSenderPort: EmailSenderPort,
        timeUtils: TimeUtils,
    ) = SendEmailTaskExecutor(properties, templateRepositoryPort, serviceTaskRepositoryPort, emailSenderPort, timeUtils)
}
