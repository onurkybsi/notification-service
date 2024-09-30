package org.kybprototyping.notificationservice.domain

import org.kybprototyping.notificationservice.domain.common.UseCaseHandler
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.*
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplateCreationUseCase
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplateDeletionUseCase
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplateRetrievalUseCase
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplatesRetrievalUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration("domain")
internal open class SpringConfiguration {

    @Bean
    internal open fun notificationTemplatesRetrievalUseCase(
        repositoryPort: NotificationTemplateRepositoryPort
    ): UseCaseHandler<NotificationTemplatesRetrievalInput, List<NotificationTemplate>> =
        NotificationTemplatesRetrievalUseCase(repositoryPort)

    @Bean
    internal open fun notificationTemplateRetrievalUseCase(repositoryPort: NotificationTemplateRepositoryPort): UseCaseHandler<Int, NotificationTemplate?> =
        NotificationTemplateRetrievalUseCase(repositoryPort)

    @Bean
    internal open fun notificationTemplateCreationUseCase(repositoryPort: NotificationTemplateRepositoryPort) =
        NotificationTemplateCreationUseCase(repositoryPort)

    @Bean
    internal open fun notificationTemplateDeletionUseCase(repositoryPort: NotificationTemplateRepositoryPort) =
        NotificationTemplateDeletionUseCase(repositoryPort)

    @Bean
    internal open fun notificationTemplateUpdateUseCase(repositoryPort: NotificationTemplateRepositoryPort) =
        NotificationTemplateUpdateUseCase(repositoryPort)

}