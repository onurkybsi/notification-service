package org.kybprototyping.notificationservice.domain

import org.kybprototying.notificationservice.common.TimeUtils
import org.kybprototyping.notificationservice.domain.common.CoroutineDispatcherProvider
import org.kybprototyping.notificationservice.domain.common.UseCaseHandler
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate
import org.kybprototyping.notificationservice.domain.model.ServiceTaskType
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort
import org.kybprototyping.notificationservice.domain.port.ServiceTaskRepositoryPort
import org.kybprototyping.notificationservice.domain.scheduled.servicetask.ServiceTaskExecutor
import org.kybprototyping.notificationservice.domain.scheduled.servicetask.ServiceTaskExecutorJob
import org.kybprototyping.notificationservice.domain.usecase.notification.SendEmailUseCase
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplateCreationUseCase
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplateDeletionUseCase
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplateRetrievalUseCase
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplateUpdateUseCase
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplatesRetrievalInput
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplatesRetrievalUseCase
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
        executors: Map<ServiceTaskType, ServiceTaskExecutor>,
    ) = ServiceTaskExecutorJob(coroutineDispatcherProvider, timeUtils, serviceTaskRepositoryPort, executors)

    @Bean
    internal open fun sendEmailUseCase(
        timeUtils: TimeUtils,
        serviceTaskRepositoryPort: ServiceTaskRepositoryPort,
    ) = SendEmailUseCase(timeUtils, serviceTaskRepositoryPort)
}
