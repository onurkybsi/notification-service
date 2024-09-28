package org.kybprototyping.notificationservice.adapter.repository.notificationtemplate

import io.kotest.assertions.arrow.core.shouldBeRight
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kybprototyping.notificationservice.adapter.TestData
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate as DomainNotificationTemplate
import org.kybprototyping.notificationservice.domain.model.NotificationType as DomainNotificationType
import org.kybprototyping.notificationservice.adapter.repository.PostgreSQLContainerRunner
import org.kybprototyping.notificationservice.adapter.repository.common.DbSpringConfiguration
import org.kybprototyping.notificationservice.domain.model.NotificationType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.allAndAwait
import org.springframework.data.r2dbc.core.delete

@SpringBootTest(
    classes = [
        DbSpringConfiguration::class,
        SpringDataImpl::class
    ]
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
        insert(TestData.notificationTemplate.copy(id = 2, type = DomainNotificationType.PASSWORD_RESET))

        // when
        val actual = underTest.findBy(null, null, null)

        // then
        actual shouldBeRight listOf(TestData.notificationTemplate, TestData.notificationTemplate.copy(id = 2, type = DomainNotificationType.PASSWORD_RESET))
    }

    @Test
    fun `should return notification templates by notification type`() = runTest {
        // given
        insert(TestData.notificationTemplate)
        insert(TestData.notificationTemplate.copy(id = 2, type = DomainNotificationType.PASSWORD_RESET))

        // when
        val actual = underTest.findBy(null, NotificationType.WELCOME, null)

        // then
        actual shouldBeRight listOf(TestData.notificationTemplate)
    }

    private suspend fun insert(template: DomainNotificationTemplate) {
        entityTemplate.insert(NotificationTemplate.from(template)).awaitSingleOrNull() ?: throw AssertionError()
    }

}