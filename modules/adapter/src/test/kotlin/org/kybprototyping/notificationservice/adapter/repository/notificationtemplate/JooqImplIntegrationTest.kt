package org.kybprototyping.notificationservice.adapter.repository.notificationtemplate

import arrow.core.getOrElse
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.kybprototyping.notificationservice.adapter.TestData
import org.kybprototyping.notificationservice.adapter.repository.PostgreSQLContainerRunner
import org.kybprototyping.notificationservice.adapter.repository.common.DslContextSpringConfiguration
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.JooqImpl.Companion.toDomain
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.JooqImpl.Companion.toRecordForCreation
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.tables.references.NOTIFICATION_TEMPLATE
import org.kybprototyping.notificationservice.domain.model.NotificationChannel
import org.kybprototyping.notificationservice.domain.model.NotificationLanguage
import org.kybprototyping.notificationservice.domain.model.NotificationType
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate as DomainNotificationTemplate

@SpringBootTest(
    classes = [
        R2dbcAutoConfiguration::class,
        DslContextSpringConfiguration::class,
        JooqImpl::class
    ],
    properties = [
        "ports.notification-template-repository.impl=jooq"
    ]
)
internal class JooqImplIntegrationTest : PostgreSQLContainerRunner() {

    @Autowired
    private lateinit var dslContext: DSLContext

    @Autowired
    private lateinit var underTest: JooqImpl

    @BeforeEach
    fun setUp() {
        runBlocking {
            dslContext.delete(NOTIFICATION_TEMPLATE).awaitSingle()
        }
    }

    @Test
    fun `should return notification template from repository by ID`() = runTest {
        // given
        insert(TestData.notificationTemplate)
        val id = 1

        // when
        val actual = underTest.findById(id)

        // then
        actual shouldBeRight TestData.notificationTemplate
    }

    @Test
    fun `should return null when no notification template found with given ID in repository`() = runTest {
        // given
        val id = 1

        // when
        val actual = underTest.findById(id)

        // then
        actual shouldBeRight null
    }

    @Test
    fun `should return all notification templates`() = runTest {
        // given
        insert(TestData.notificationTemplate)
        insert(TestData.notificationTemplate.copy(id = 2, type = NotificationType.PASSWORD_RESET))

        // when
        val actual = underTest.findBy(null, null, null)

        // then
        actual shouldBeRight listOf(TestData.notificationTemplate, TestData.notificationTemplate.copy(id = 2, type = NotificationType.PASSWORD_RESET))
    }

    @Test
    fun `should return notification templates by notification channel`() = runTest {
        // given
        insert(TestData.notificationTemplate)

        // when
        val actual = underTest.findBy(NotificationChannel.EMAIL, null, null)

        // then
        actual shouldBeRight listOf(TestData.notificationTemplate)
    }

    @Test
    fun `should return notification templates by notification type`() = runTest {
        // given
        insert(TestData.notificationTemplate)
        insert(TestData.notificationTemplate.copy(id = 2, type = NotificationType.PASSWORD_RESET))

        // when
        val actual = underTest.findBy(null, NotificationType.WELCOME, null)

        // then
        actual shouldBeRight listOf(TestData.notificationTemplate)
    }

    @Test
    fun `should return notification templates by notification language`() = runTest {
        // given
        insert(TestData.notificationTemplate)

        // when
        val actual = underTest.findBy(null, null, NotificationLanguage.EN)

        // then
        actual shouldBeRight listOf(TestData.notificationTemplate)
    }

    @Test
    fun `should create notification template by values`() = runTest {
        // given

        // when
        val actual = underTest.create(
            channel = TestData.notificationTemplate.channel,
            type = TestData.notificationTemplate.type,
            language = TestData.notificationTemplate.language,
            subject = TestData.notificationTemplate.subject,
            content = TestData.notificationTemplate.content
        )

        // then
        actual shouldBeRight 1
        val created = underTest.findBy(
            channel = TestData.notificationTemplate.channel,
            type = TestData.notificationTemplate.type,
            language = TestData.notificationTemplate.language
        ).getOrElse { throw AssertionError() }
        Assertions.assertThat(created[0])
            .usingRecursiveComparison()
            .ignoringFields("modifiedAt", "createdAt") // TODO: Assert these as well after TimeUtils!
            .isEqualTo(TestData.notificationTemplate)
    }

    @Test
    fun `should return null for notification template creation when a template is already created with the same channel, type and language`() = runTest {
        // given
        insert(TestData.notificationTemplate)

        // when
        val actual = underTest.create(
            channel = TestData.notificationTemplate.channel,
            type = TestData.notificationTemplate.type,
            language = TestData.notificationTemplate.language,
            subject = TestData.notificationTemplate.subject,
            content = TestData.notificationTemplate.content
        )

        // then
        actual shouldBeRight null
    }

    @Test
    fun `should delete an existing notification template from data repository by given ID`() = runTest {
        // given
        val id = TestData.notificationTemplate.id
        insert(TestData.notificationTemplate)

        // when
        val actual = underTest.delete(id)

        // then
        actual shouldBeRight Unit
        val templateCount = dslContext.selectCount().from(NOTIFICATION_TEMPLATE).awaitSingle().map { it.get(0, Int::class.java)}
        Assertions.assertThat(templateCount).isEqualTo(0)
    }

    @Test
    fun `should return DataNotFoundFailure for notification template deletion when no template found by given ID`() = runTest {
        // given
        val id = TestData.notificationTemplate.id

        // when
        val actual = underTest.delete(id)

        // then
        actual shouldBeLeft NotificationTemplateRepositoryPort.DeletionFailure.DataNotFoundFailure
    }

    @ParameterizedTest
    @CsvSource(value = [
        "Updated Subject, Updated Subject, Updated Content, Updated Content",
        "Updated Subject, Updated Subject, null, Welcome to our platform \${firstName}",
        "null, Welcome, Updated Content, Updated Content",
        "null, Welcome, null, Welcome to our platform \${firstName}",
    ], nullValues = ["null"])
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
        val updated = underTest.findBy(
            channel = TestData.notificationTemplate.channel,
            type = TestData.notificationTemplate.type,
            language = TestData.notificationTemplate.language
        ).getOrElse { throw AssertionError() }
        Assertions.assertThat(updated[0])
            .usingRecursiveComparison()
            .ignoringFields("modifiedAt", "createdAt") // TODO: Assert these as well after TimeUtils!
            .isEqualTo(TestData.notificationTemplate.copy(subject = expectedSubject, content = expectedContent))
    }

    @Test
    fun `should return DataNotFoundFailure for notification template update when no template found by given ID`() = runTest {
        // given
        val id = TestData.notificationTemplate.id

        // when
        val actual = underTest.update(id, "Updated Subject", "Updated Content")

        // then
        actual shouldBeLeft NotificationTemplateRepositoryPort.UpdateFailure.DataNotFoundFailure
    }

    private suspend fun insert(template: DomainNotificationTemplate) =
        dslContext
        .insertInto(NOTIFICATION_TEMPLATE)
        .set(template.toRecordForCreation().also { it.id = template.id })
        .returning()
        .awaitSingle()
        .toDomain()

}