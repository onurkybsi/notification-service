package org.kybprototyping.notificationservice.adapter.repository.common

import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.ReactiveTransactionManager

@Configuration
internal class TransactionalExecutorSpringConfiguration {
    @Bean
    internal fun transactionalExecutorSpringTransactionManagerImpl(
        connectionFactory: ConnectionFactory,
        transactionManager: ReactiveTransactionManager,
    ) = TransactionalExecutorSpringTransactionManagerImpl(connectionFactory, transactionManager)

    @Bean
    @ConditionalOnProperty(
        value = ["ports.notification-template-repository.impl"],
        havingValue = "jooq",
    )
    internal fun transactionAwareDSLContextProxy(connectionFactory: ConnectionFactory) = TransactionAwareDSLContextProxy(connectionFactory)
}
