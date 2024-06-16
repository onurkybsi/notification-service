package org.kybprototyping.notificationservice.adapter.notificationtemplaterepository.spring

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.kybprototyping.notificationservice.adapter.common.DatabaseClientSpringConfiguration
import org.kybprototyping.notificationservice.domain.model.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import org.springframework.r2dbc.core.awaitSingleOrNull
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import java.time.OffsetDateTime

@SpringBootTest(classes = [DatabaseClientSpringConfiguration::class, NotificationTemplateMapper::class, SpringDataClientAdapter::class])
@ActiveProfiles("test")
internal class SpringDataClientAdapterIntegrationTest {

    @Autowired
    private lateinit var databaseClient: DatabaseClient

    @Autowired
    private lateinit var underTest: SpringDataClientAdapter

    @BeforeEach
    fun refresh() = runBlocking {
        databaseClient.sql("DELETE FROM public.notification_template").await()
    }

    @Test
    fun `create should insert to notification_template by given request`() = runTest {
        // given
        val request = NotificationTemplateCreationRequest(
            channel = NotificationChannel.EMAIL,
            type = NotificationType.WELCOME,
            language = NotificationLanguage.EN,
            subject = "subject",
            content = "content"
        )

        // when
        val actual = underTest.create(request)

        // then
        assertNotNull(actual)
    }

    @ParameterizedTest
    @MethodSource("getListByArguments")
    fun `getListBy should return notification templates with given values`(
        testTemplateCreationRequests: Array<NotificationTemplateCreationRequest>,
        channel: NotificationChannel?,
        type: NotificationType?,
        language: NotificationLanguage?,
        expected: List<NotificationTemplate>
    ) = runTest {
        // given
        testTemplateCreationRequests.forEach { underTest.create(it) }

        // when
        val actual = underTest.getListBy(channel, type, language)

        // then
        assertTrue(
            actual.zip(expected).all { (a, e) ->
                a.channel == e.channel &&
                a.type == e.type &&
                a.language == e.language &&
                a.content == e.content &&
                a.modifiedBy == e.modifiedBy &&
                a.createdBy == e.createdBy
        })
    }

    @ParameterizedTest
    @MethodSource("getOneByArguments")
    fun `getOneBy should return notification template with given values`(
        testTemplateCreationRequests: Array<NotificationTemplateCreationRequest>,
        channel: NotificationChannel,
        type: NotificationType,
        language: NotificationLanguage,
        expected: NotificationTemplate?
    ) = runTest {
        // given
        testTemplateCreationRequests.forEach { underTest.create(it) }

        // when
        val actual = underTest.getOneBy(channel, type, language)

        // then
        assertThat(actual)
            .usingRecursiveComparison()
            .ignoringFields("id", "modificationDate", "creationDate")
            .isEqualTo(expected)
    }

    @Test
    fun `getById should return notification template with given ID`() = runTest {
        // given
        val createdTemplateId = underTest.create(notificationTemplateCreationRequest())

        // when
        val actual = underTest.getById(createdTemplateId)

        // then
        val expected = notificationTemplate().copy(id = createdTemplateId)
        assertThat(actual)
            .usingRecursiveComparison()
            .ignoringFields("modificationDate", "creationDate")
            .isEqualTo(expected)
    }

    @Test
    fun `getById should return null when no notification template found with given ID`() = runTest {
        // given
        val id = 1

        // when
        val actual = underTest.getById(id)

        // then
        assertThat(actual).isNull()
    }

    @Test
    fun `updateBy should update notification template by given request and return updated`() = runTest {
        // given
        val createTemplateId = underTest.create(notificationTemplateCreationRequest())

        // when
        val actualResult = underTest.updateBy(NotificationTemplateUpdateRequest(
            id = createTemplateId,
            subject = "updated subject",
            content = "updated content"
        ))

        // then
        assertThat(actualResult)
            .usingRecursiveComparison()
            .ignoringFields("id","modificationDate", "creationDate")
            .isEqualTo(notificationTemplate().copy(subject = "updated subject", content = "updated content"))
    }

    @Test
    fun `updateBy should return null when no notification template found with given ID`() = runTest {
        // given
        val id = 1

        // when
        val actualResult = underTest.updateBy(NotificationTemplateUpdateRequest(id, null, null))

        // then
        assertThat(actualResult).isNull()
    }

    @Test
    fun `delete should delete notification template with given ID and return updated`() = runTest {
        // given
        val createTemplateId = underTest.create(notificationTemplateCreationRequest())
        val createdTemplate = underTest.getById(createTemplateId)

        // when
        val actualResult = underTest.delete(createTemplateId)

        // then
        assertThat(actualResult).isEqualTo(createdTemplate)
        val templatesCount = databaseClient
            .sql("SELECT COUNT(*) FROM public.notification_template")
            .map { row -> row.get(0) as Long }
            .awaitSingleOrNull()
        assertThat(templatesCount).isEqualTo(0)
    }

    @Test
    fun `delete should return null when no notification template found with given ID`() = runTest {
        // given
        val id = 1

        // when
        val actualResult = underTest.delete(id)

        // then
        assertThat(actualResult).isNull()
        val templatesCount = databaseClient
            .sql("SELECT COUNT(*) FROM public.notification_template")
            .map { row -> row.get(0) as Long }
            .awaitSingleOrNull()
        assertThat(templatesCount).isEqualTo(0)
    }

    private companion object {
        private val instance = PostgreSQLContainer("postgres:15.5")
            .withDatabaseName("notification_db")

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

        private fun notificationTemplateCreationRequest() = NotificationTemplateCreationRequest(
            channel = NotificationChannel.EMAIL,
            type = NotificationType.WELCOME,
            language = NotificationLanguage.EN,
            subject = "subject",
            content = "content"
        )

        private fun notificationTemplate() = NotificationTemplate(
            id = 1,
            channel = NotificationChannel.EMAIL,
            type = NotificationType.WELCOME,
            language = NotificationLanguage.EN,
            subject = "subject",
            content = "content",
            modifiedBy = null,
            modificationDate = OffsetDateTime.parse("2024-06-01T09:00:00Z"),
            createdBy = null,
            creationDate = OffsetDateTime.parse("2024-06-01T09:00:00Z"),
        )

        @JvmStatic
        fun getListByArguments() = listOf(
            Arguments.of(
                arrayOf(
                    notificationTemplateCreationRequest(),
                    notificationTemplateCreationRequest().copy(type = NotificationType.PASSWORD_RESET)
                ),
                NotificationChannel.EMAIL,
                null,
                null,
                listOf(
                    notificationTemplate(),
                    notificationTemplate().copy(type = NotificationType.PASSWORD_RESET)
                )
            ),
            Arguments.of(
                arrayOf(
                    notificationTemplateCreationRequest(),
                    notificationTemplateCreationRequest().copy(type = NotificationType.PASSWORD_RESET)
                ),
                null,
                NotificationType.WELCOME,
                null,
                listOf(
                    notificationTemplate()
                )
            ),
            Arguments.of(
                arrayOf(
                    notificationTemplateCreationRequest(),
                    notificationTemplateCreationRequest().copy(type = NotificationType.PASSWORD_RESET)
                ),
                null,
                null,
                NotificationLanguage.EN,
                listOf(
                    notificationTemplate(),
                    notificationTemplate().copy(type = NotificationType.PASSWORD_RESET)
                )
            ),
            Arguments.of(
                arrayOf(
                    notificationTemplateCreationRequest(),
                    notificationTemplateCreationRequest().copy(type = NotificationType.PASSWORD_RESET)
                ),
                NotificationChannel.EMAIL,
                NotificationType.WELCOME,
                NotificationLanguage.EN,
                listOf(
                    notificationTemplate()
                )
            ),
            Arguments.of(
                arrayOf(
                    notificationTemplateCreationRequest()
                ),
                null,
                NotificationType.PASSWORD_RESET,
                null,
                emptyList<NotificationTemplate>()
            ),
            Arguments.of(
                arrayOf(
                    notificationTemplateCreationRequest(),
                    notificationTemplateCreationRequest().copy(type = NotificationType.PASSWORD_RESET)
                ),
                null,
                null,
                null,
                listOf(
                    notificationTemplate(),
                    notificationTemplate().copy(type = NotificationType.PASSWORD_RESET)
                )
            )
        )

        @JvmStatic
        fun getOneByArguments() = listOf(
            Arguments.of(
                arrayOf(
                    notificationTemplateCreationRequest(),
                    notificationTemplateCreationRequest().copy(type = NotificationType.PASSWORD_RESET)
                ),
                NotificationChannel.EMAIL,
                NotificationType.WELCOME,
                NotificationLanguage.EN,
                notificationTemplate()
            ),
            Arguments.of(
                arrayOf(
                    notificationTemplateCreationRequest()
                ),
                NotificationChannel.EMAIL,
                NotificationType.PASSWORD_RESET,
                NotificationLanguage.EN,
                null
            )
        )
    }

}