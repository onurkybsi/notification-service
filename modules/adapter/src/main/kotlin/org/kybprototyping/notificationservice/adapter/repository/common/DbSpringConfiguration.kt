package org.kybprototyping.notificationservice.adapter.repository.common

import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate

@Configuration
@EnableConfigurationProperties(DbSpringConfiguration.DbProperties::class)
internal class DbSpringConfiguration {

    @Autowired
    private lateinit var dbProperties: DbProperties

    // TODO: Can we use the auto config again?
    @Bean
    internal fun connectionFactory(): ConnectionFactory =
        PostgresqlConnectionFactory(
            PostgresqlConnectionConfiguration.builder()
                .host(dbProperties.host)
                .port(dbProperties.port)
                .database("notification_db")
                .username(dbProperties.user)
                .password(dbProperties.password)
                .build()
        ).let { ConnectionPool(ConnectionPoolConfiguration.builder(it).maxSize(dbProperties.poolMaxSize).build()) }

    @Bean
    internal fun r2dbcEntityTemplate(connectionFactory: ConnectionFactory) = R2dbcEntityTemplate(connectionFactory)

    @ConfigurationProperties(prefix = "db")
    internal data class DbProperties @ConstructorBinding constructor(
        val host: String,
        val port: Int,
        val user: String,
        val password: String,
        val poolMaxSize: Int
    )

}