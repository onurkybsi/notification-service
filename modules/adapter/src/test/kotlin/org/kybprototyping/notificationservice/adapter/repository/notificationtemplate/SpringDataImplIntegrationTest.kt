package org.kybprototyping.notificationservice.adapter.repository.notificationtemplate

import arrow.core.getOrElse
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.kybprototyping.notificationservice.adapter.TestData
import org.kybprototyping.notificationservice.adapter.repository.PostgreSQLContainerRunner
import org.kybprototyping.notificationservice.domain.model.NotificationType
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.allAndAwait
import org.springframework.data.r2dbc.core.delete
import org.springframework.data.relational.core.query.Query
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate as DomainNotificationTemplate
import org.kybprototyping.notificationservice.domain.model.NotificationType as DomainNotificationType

@SpringBootTest(
    classes = [
        R2dbcAutoConfiguration::class,
        R2dbcDataAutoConfiguration::class,
        SpringDataImpl::class,
    ],
    properties = [
        "ports.notification-template-repository.impl=spring-data",
    ],
)
internal class SpringDataImplIntegrationTest : PostgreSQLContainerRunner() {
    @Autowired
    private lateinit var entityTemplate: R2dbcEntityTemplate

    @Autowired
    private lateinit var underTest: SpringDataImpl

    @BeforeEach
    fun setUp() {
        runBlocking {
            entityTemplate.delete<NotificationTemplate>().allAndAwait()
        }
    }

    @Test
    fun `should return notification template from repository by ID`() =
        runTest {
            // given
            insert(TestData.notificationTemplate)
            val id = 1

            // when
            val actual = underTest.findById(id)

            // then
            actual shouldBeRight TestData.notificationTemplate
        }

    @Test
    fun `should return null when no notification template found with given ID in repository`() =
        runTest {
            // given
            val id = 1

            // when
            val actual = underTest.findById(id)

            // then
            actual shouldBeRight null
        }

    @Test
    fun `should return all notification templates`() =
        runTest {
            // given
            insert(TestData.notificationTemplate)
            insert(TestData.notificationTemplate.copy(id = 2, type = DomainNotificationType.PASSWORD_RESET))

            // when
            val actual = underTest.findBy(null, null, null)

            // then
            actual.shouldBeRight(
                listOf(
                    TestData.notificationTemplate,
                    TestData.notificationTemplate.copy(id = 2, type = DomainNotificationType.PASSWORD_RESET),
                ),
            )
        }

    @Test
    fun `should return notification templates by notification type`() =
        runTest {
            // given
            insert(TestData.notificationTemplate)
            insert(TestData.notificationTemplate.copy(id = 2, type = DomainNotificationType.PASSWORD_RESET))

            // when
            val actual = underTest.findBy(null, NotificationType.WELCOME, null)

            // then
            actual shouldBeRight listOf(TestData.notificationTemplate)
        }

    @Test
    fun `should create notification template by values`() =
        runTest {
            // given

            // when
            val actual =
                underTest.create(
                    channel = TestData.notificationTemplate.channel,
                    type = TestData.notificationTemplate.type,
                    language = TestData.notificationTemplate.language,
                    subject = TestData.notificationTemplate.subject,
                    content = TestData.notificationTemplate.content,
                )

            // then
            actual shouldBeRight 1
            val created =
                underTest.findBy(
                    channel = TestData.notificationTemplate.channel,
                    type = TestData.notificationTemplate.type,
                    language = TestData.notificationTemplate.language,
                ).getOrElse { throw AssertionError() }
            assertThat(created[0])
                .usingRecursiveComparison()
                .ignoringFields("modifiedAt", "createdAt") // TODO: Assert these as well after TimeUtils!
                .isEqualTo(TestData.notificationTemplate)
        }

    @Test
    fun `should return null for notification template creation when a template is already created with the same channel, type and language`() =
        runTest {
            // given
            insert(TestData.notificationTemplate)

            // when
            val actual =
                underTest.create(
                    channel = TestData.notificationTemplate.channel,
                    type = TestData.notificationTemplate.type,
                    language = TestData.notificationTemplate.language,
                    subject = TestData.notificationTemplate.subject,
                    content = TestData.notificationTemplate.content,
                )

            // then
            actual shouldBeRight null
        }

    @Test
    fun `should delete an existing notification template from data repository by given ID`() =
        runTest {
            // given
            val id = TestData.notificationTemplate.id
            insert(TestData.notificationTemplate)

            // when
            val actual = underTest.delete(id)

            // then
            actual shouldBeRight Unit
            val templateCount = entityTemplate.count(Query.empty(), NotificationTemplate::class.java).awaitSingle()
            assertThat(templateCount).isEqualTo(0)
        }

    @Test
    fun `should return DataNotFoundFailure for notification template deletion when no template found by given ID`() =
        runTest {
            // given
            val id = TestData.notificationTemplate.id

            // when
            val actual = underTest.delete(id)

            // then
            actual shouldBeLeft NotificationTemplateRepositoryPort.DeletionFailure.DataNotFoundFailure
        }

    @ParameterizedTest
    @CsvSource(
        value = [
            "Updated Subject, Updated Subject, Updated Content, Updated Content",
            "Updated Subject, Updated Subject, null, Welcome to our platform \${firstName}",
            "null, Welcome, Updated Content, Updated Content",
            "null, Welcome, null, Welcome to our platform \${firstName}",
        ],
        nullValues = ["null"],
    )
    fun `should update an existing notification template by values`(
        subjectToSet: String?,
        expectedSubject: String,
        contentToSet: String?,
        expectedContent: String,
    ) = runTest {
        // given
        val id = TestData.notificationTemplate.id
        insert(TestData.notificationTemplate)

        // when
        val actual = underTest.update(id, subjectToSet, contentToSet)

        // then
        actual shouldBeRight Unit
        val updated =
            underTest.findBy(
                channel = TestData.notificationTemplate.channel,
                type = TestData.notificationTemplate.type,
                language = TestData.notificationTemplate.language,
            ).getOrElse { throw AssertionError() }
        assertThat(updated[0])
            .usingRecursiveComparison()
            .ignoringFields("modifiedAt", "createdAt") // TODO: Assert these as well after TimeUtils!
            .isEqualTo(TestData.notificationTemplate.copy(subject = expectedSubject, content = expectedContent))
    }

    @Test
    fun `should return DataNotFoundFailure for notification template update when no template found by given ID`() =
        runTest {
            // given
            val id = TestData.notificationTemplate.id

            // when
            val actual = underTest.update(id, "Updated Subject", "Updated Content")

            // then
            actual shouldBeLeft NotificationTemplateRepositoryPort.UpdateFailure.DataNotFoundFailure
        }

    private suspend fun insert(template: DomainNotificationTemplate) {
        entityTemplate.insert(NotificationTemplate.from(template)).awaitSingleOrNull() ?: throw AssertionError()
    }
}
