package org.kybprototyping.notificationservice.domain

import arrow.core.Either
import org.kybprototyping.notificationservice.domain.common.Failure
import org.kybprototyping.notificationservice.domain.common.UseCaseHandler
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplateRetrievalUseCase
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplatesRetrievalInput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration("domain")
internal open class SpringConfiguration {

    @Bean
    internal open fun notificationTemplatesRetrievalUseCase(): UseCaseHandler<NotificationTemplatesRetrievalInput, List<NotificationTemplate>> {
        return object : UseCaseHandler<NotificationTemplatesRetrievalInput, List<NotificationTemplate>> {
            override suspend fun handle(input: NotificationTemplatesRetrievalInput): Either<Failure, List<NotificationTemplate>> {
                TODO("Not yet implemented")
            }

        }
    }

    @Bean
    internal open fun notificationTemplateRetrievalUseCase(): UseCaseHandler<Int, NotificationTemplate> {
        return NotificationTemplateRetrievalUseCase()
    }

}