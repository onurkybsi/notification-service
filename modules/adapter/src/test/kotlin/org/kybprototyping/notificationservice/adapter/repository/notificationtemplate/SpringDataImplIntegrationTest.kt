package org.kybprototyping.notificationservice.adapter.repository.notificationtemplate

import io.kotest.assertions.arrow.core.shouldBeRight
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kybprototyping.notificationservice.adapter.TestData
import org.kybprototyping.notificationservice.adapter.repository.PostgreSQLContainerRunner
import org.kybprototyping.notificationservice.adapter.repository.common.DbSpringConfiguration
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
        entityTemplate.insert(NotificationTemplate.from(TestData.notificationTemplate)).awaitSingleOrNull() ?: throw AssertionError()
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

}