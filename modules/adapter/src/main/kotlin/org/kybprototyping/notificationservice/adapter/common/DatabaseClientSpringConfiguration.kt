package org.kybprototyping.notificationservice.adapter.common

import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.codec.EnumCodec
import io.r2dbc.spi.ConnectionFactory
import org.kybprototyping.notificationservice.adapter.notificationtemplaterepository.spring.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy
import org.springframework.r2dbc.core.DatabaseClient

@Configuration
open class DatabaseClientSpringConfiguration {

    @Bean
    internal open fun databaseClient(connectionFactory: ConnectionFactory) = DatabaseClient.create(connectionFactory)

    @Bean
    internal open fun transactionAwareConnectionFactoryProxy(
        @Value("\${db.host}") dbHost: String,
        @Value("\${db.port}") dbPort: Int,
        @Value("\${db.user}") dbUser: String,
        @Value("\${db.password}") dbPassword: String,
        @Value("\${db.poolMaxSize}") dbPoolMaxSize: Int
    ): ConnectionFactory {
        val connectionFactory = PostgresqlConnectionFactory(
            PostgresqlConnectionConfiguration.builder()
                .host(dbHost)
                .port(dbPort)
                .database("notification_db")
                .username(dbUser)
                .password(dbPassword)
                .codecRegistrar(
                    EnumCodec.builder()
                        .withEnum("notification_channel", NotificationChannelEntity::class.java)
                        .withEnum("notification_type", NotificationTypeEntity::class.java)
                        .withEnum("notification_language", NotificationLanguageEntity::class.java)
                        .build()
                )
                .build()
        )
        val poolConfiguration = ConnectionPoolConfiguration.builder(connectionFactory)
            .maxSize(dbPoolMaxSize)
            .build()
        return TransactionAwareConnectionFactoryProxy(ConnectionPool(poolConfiguration))
    }

}
