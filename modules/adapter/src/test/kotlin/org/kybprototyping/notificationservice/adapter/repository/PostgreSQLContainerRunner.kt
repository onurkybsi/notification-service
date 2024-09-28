package org.kybprototyping.notificationservice.adapter.repository

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer

internal open class PostgreSQLContainerRunner {
    private companion object {
        private val instance = PostgreSQLContainer("postgres:15.5")
            .withDatabaseName("notification_db")
            .withReuse(true)

        @BeforeAll
        @JvmStatic
        fun startDBContainer() {
            instance.start()
            flywayMigration()
        }

        @AfterAll
        @JvmStatic
        fun stopDBContainer() {
            instance.stop()
        }

        @DynamicPropertySource
        @JvmStatic
        private fun setDbProperties(registry: DynamicPropertyRegistry) {
            val host = instance.host
            val port = instance.getMappedPort(5432)
            val username = instance.username
            val password = instance.password
            registry.add("db.host") { host }
            registry.add("db.port") { port }
            registry.add("db.user") { username }
            registry.add("db.password") { password }
        }

        private fun flywayMigration() {
            val jdbcUrl = instance.jdbcUrl
            val username = instance.username
            val password = instance.password
            Flyway.configure()
                .dataSource(jdbcUrl, username, password)
                .load()
                .migrate()
        }
    }
}