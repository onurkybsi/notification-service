package org.kybprototyping.notificationservice.domain

import org.kybprototyping.notificationservice.domain.common.UseCaseHandler
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplateRetrievalUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration("domain")
internal open class SpringConfiguration {

    @Bean
    internal open fun notificationTemplateRetrievalUseCase(): UseCaseHandler<Int, NotificationTemplate> {
        return NotificationTemplateRetrievalUseCase()
    }

}