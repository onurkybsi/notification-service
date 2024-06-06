package org.kybprototyping.notificationservice.adapter.common

import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.codec.EnumCodec
import io.r2dbc.spi.ConnectionFactory
import org.kybprototyping.notificationservice.domain.model.NotificationChannel
import org.kybprototyping.notificationservice.domain.model.NotificationLanguage
import org.kybprototyping.notificationservice.domain.model.NotificationType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy
import org.springframework.r2dbc.core.DatabaseClient
import java.time.Duration

@Configuration
open class DatabaseClientSpringConfiguration {

    @Bean
    internal open fun transactionAwareConnectionFactoryProxy(): ConnectionFactory {
        val connectionFactory = PostgresqlConnectionFactory(
            PostgresqlConnectionConfiguration.builder()
                .host("localhost")
                .database("notification_db")
                .username("user")
                .password("password")
                .codecRegistrar(
                    EnumCodec.builder()
                        .withEnum("notification_channel", NotificationChannel::class.java)
                        .withEnum("notification_type", NotificationType::class.java)
                        .withEnum("notification_language", NotificationLanguage::class.java)
                        .build()
                )
                .build()
        )
        val poolConfiguration = ConnectionPoolConfiguration.builder(connectionFactory)
            .maxIdleTime(Duration.ofMillis(1000))
            .maxSize(20)
            .build()
        return TransactionAwareConnectionFactoryProxy(ConnectionPool(poolConfiguration))
    }

    @Bean
    internal open fun databaseClient(connectionFactory: ConnectionFactory) = DatabaseClient.create(connectionFactory)

}
