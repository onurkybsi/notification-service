package org.kybprototyping.notificationservice.adapter.repository.servicetask

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.kybprototying.notificationservice.common.DataConflictFailure
import org.kybprototying.notificationservice.common.UnexpectedFailure
import org.kybprototyping.notificationservice.adapter.TestData
import org.kybprototyping.notificationservice.adapter.repository.PostgreSQLContainerRunner
import org.kybprototyping.notificationservice.adapter.repository.common.TransactionAwareDSLContextProxy
import org.kybprototyping.notificationservice.adapter.repository.common.TransactionalExecutorSpringConfiguration
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.Tables.SERVICE_TASK
import org.kybprototyping.notificationservice.adapter.repository.servicetask.JooqImpl.Companion.toRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.boot.autoconfigure.r2dbc.R2dbcTransactionManagerAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Flux
import java.util.UUID

@SpringBootTest(
    classes = [
        R2dbcAutoConfiguration::class,
        R2dbcTransactionManagerAutoConfiguration::class,
        TransactionalExecutorSpringConfiguration::class,
        JooqImpl::class,
    ],
    properties = [
        "ports.notification-template-repository.impl=jooq",
    ],
)
internal class JooqImplIntegrationIntegrationTest : PostgreSQLContainerRunner() {
    @Autowired
    private lateinit var transactionAwareDSLContextProxy: TransactionAwareDSLContextProxy

    @Autowired
    private lateinit var underTest: JooqImpl

    @BeforeEach
    fun setUp() {
        runBlocking {
            transactionAwareDSLContextProxy.dslContext().delete(SERVICE_TASK).awaitSingle()
        }
    }

    @Nested
    inner class Insert {
        @Test
        fun `should insert given task into the underlying repository`() = runTest {
            // given
            val task = TestData.serviceTask

            // when
            val actual = underTest.insert(task)

            // then
            actual shouldBeRight Unit
            val tasks = Flux.from(transactionAwareDSLContextProxy.dslContext().selectFrom(SERVICE_TASK)).collectList().awaitSingle()
            assertThat(tasks.size).isEqualTo(1)
            val created = tasks[0]
            assertThat(created.id).isEqualTo(TestData.serviceTask.id)
            assertThat(created.type).isEqualTo(toRecord(TestData.serviceTask.type))
            assertThat(created.status).isEqualTo(toRecord(TestData.serviceTask.status))
            assertThat(created.externalId).isEqualTo(TestData.serviceTask.externalId)
            assertThat(created.priority).isEqualTo(TestData.serviceTask.priority.value().toShort())
            assertThat(created.executionCount).isEqualTo(TestData.serviceTask.executionCount.toShort())
            assertThat(created.executionStartedAt).isEqualTo(TestData.serviceTask.executionStartedAt?.toLocalDateTime())
            assertThat(created.executionScheduledAt).isEqualTo(TestData.serviceTask.executionScheduledAt?.toLocalDateTime())
            assertThat(created.input.data()).isEqualTo("{\"inputField\": \"inputValue\"}")
            assertThat(created.output).isNull()
            assertThat(created.message).isEqualTo(TestData.serviceTask.message)
            assertThat(created.modifiedAt).isEqualTo(TestData.serviceTask.modifiedAt.toLocalDateTime())
            assertThat(created.createdAt).isEqualTo(TestData.serviceTask.createdAt.toLocalDateTime())
        }

        @Test
        fun `should fail with DataConflictFailure when there is already a task inserted with the same external ID`() = runTest {
            // given
            val task = TestData.serviceTask
            underTest.insert(task.copy(id = UUID.randomUUID()))

            // when
            val actual = underTest.insert(task)

            // then
            actual shouldBeLeft DataConflictFailure("External ID already exists: ${task.externalId}")
        }

        @Test
        fun `should fail with UnexpectedFailure when there is already a task inserted with the ID`() = runTest {
            // given
            val task = TestData.serviceTask
            underTest.insert(task.copy(externalId = UUID.randomUUID()))

            // when
            val actual = underTest.insert(task)

            // then
            actual shouldBeLeft UnexpectedFailure("Primary key conflict: ${task.id}")
        }
    }

}