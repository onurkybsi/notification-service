package org.kybprototyping.notificationservice.adapter.repository.common

import io.r2dbc.spi.ConnectionFactory
import org.jooq.impl.DSL
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(
    value = ["ports.notification-template-repository.impl"],
    havingValue = "jooq"
)
internal class DslContextSpringConfiguration {

    @Bean
    internal fun dslContext(connectionFactory: ConnectionFactory) = DSL.using(connectionFactory)

}