package org.kybprototyping.notificationservice.adapter.repository.servicetask

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import kotlinx.coroutines.reactive.awaitFirstOrNull
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
import org.kybprototyping.notificationservice.adapter.TimeUtilsSpringConfiguration
import org.kybprototyping.notificationservice.adapter.repository.PostgreSQLContainerRunner
import org.kybprototyping.notificationservice.adapter.repository.common.TransactionAwareDSLContextProxy
import org.kybprototyping.notificationservice.adapter.repository.common.TransactionalExecutorSpringConfiguration
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.Tables.SERVICE_TASK
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.tables.records.ServiceTaskRecord
import org.kybprototyping.notificationservice.adapter.repository.servicetask.JooqImpl.Companion.toRecord
import org.kybprototyping.notificationservice.domain.model.ServiceTaskStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.boot.autoconfigure.r2dbc.R2dbcTransactionManagerAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.enums.ServiceTaskStatus as RecordServiceTaskStatus
import reactor.core.publisher.Flux
import java.time.OffsetDateTime
import java.util.UUID

@SpringBootTest(
    classes = [
        R2dbcAutoConfiguration::class,
        R2dbcTransactionManagerAutoConfiguration::class,
        TransactionalExecutorSpringConfiguration::class,
        TimeUtilsSpringConfiguration::class,
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
            assertThat(created.context.data()).isEqualTo("{\"input\": {\"inputField\": \"inputValue\"}}")
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

    @Nested
    inner class UpdateByStatusesAndExecutionScheduledAt {
        @Test
        fun `should update tasks with given statuses and execution scheduled time less than or equal to given or null`() = runTest {
            // given
            val task1ToUpdate = TestData.serviceTaskRecord()
            val task2ToUpdate = TestData.serviceTaskRecord(
                executionScheduledAt = OffsetDateTime.parse("2024-10-01T09:00:00Z").toLocalDateTime()
            )
            val task3ToUpdate = TestData.serviceTaskRecord(
                status = RecordServiceTaskStatus.ERROR,
                executionScheduledAt = OffsetDateTime.parse("2024-10-01T08:50:00Z").toLocalDateTime()
            )
            insert(task1ToUpdate)
            insert(task2ToUpdate)
            insert(task3ToUpdate)
            insert(TestData.serviceTaskRecord(status = RecordServiceTaskStatus.COMPLETED))
            insert(TestData.serviceTaskRecord(executionScheduledAt = OffsetDateTime.parse("2024-10-01T09:00:01Z").toLocalDateTime()))
            val statuses = listOf(ServiceTaskStatus.PENDING, ServiceTaskStatus.ERROR)
            val executionScheduledAt = OffsetDateTime.parse("2024-10-01T09:00:00Z")
            val statusToSet = ServiceTaskStatus.IN_PROGRESS
            val executionScheduledAtToSet = null
            val executionStartedAtToSet = OffsetDateTime.parse("2025-01-01T00:00Z")

            // when
            val actual = underTest.updateBy(statuses, executionScheduledAt, statusToSet, executionScheduledAtToSet, executionStartedAtToSet)

            // then
            actual
                .shouldBeRight()
                .also { updatedTasks ->
                    assertThat(updatedTasks)
                        .isEqualTo(
                            listOf(
                                TestData.serviceTask.copy(
                                    id = task1ToUpdate.id,
                                    status = ServiceTaskStatus.IN_PROGRESS,
                                    externalId = task1ToUpdate.externalId,
                                    executionStartedAt = OffsetDateTime.parse("2025-01-01T00:00Z"),
                                    executionScheduledAt = null,
                                    modifiedAt = OffsetDateTime.parse("2025-01-01T12:00Z")
                                ),
                                TestData.serviceTask.copy(
                                    id = task2ToUpdate.id,
                                    status = ServiceTaskStatus.IN_PROGRESS,
                                    externalId = task2ToUpdate.externalId,
                                    executionStartedAt = OffsetDateTime.parse("2025-01-01T00:00Z"),
                                    executionScheduledAt = null,
                                    modifiedAt = OffsetDateTime.parse("2025-01-01T12:00Z")
                                ),
                                TestData.serviceTask.copy(
                                    id = task3ToUpdate.id,
                                    status = ServiceTaskStatus.IN_PROGRESS,
                                    externalId = task3ToUpdate.externalId,
                                    executionStartedAt = OffsetDateTime.parse("2025-01-01T00:00Z"),
                                    executionScheduledAt = null,
                                    modifiedAt = OffsetDateTime.parse("2025-01-01T12:00Z")
                                ),
                            )
                        )
                }
        }
    }

    @Nested
    inner class UpdateById {
        @Test
        fun `should update the service task with given ID by given values`() = runTest {
            // given
            val taskToUpdate = TestData.serviceTaskRecord()
            insert(taskToUpdate)
            val statusToSet = ServiceTaskStatus.COMPLETED
            val executionCountToSet = 1
            val executionStartedAtToSet: OffsetDateTime? = null
            val executionScheduledAtToSet: OffsetDateTime? = null
            val contextToSet = objectMapper
                .createObjectNode()
                .set<ObjectNode>("output", objectMapper.createObjectNode().set("status", TextNode("SUCCESSFUL")))
            val messageToSet: String? = null

            // when
            val actual = underTest.updateBy(
                id = taskToUpdate.id,
                statusToSet = statusToSet,
                executionCountToSet = executionCountToSet,
                executionStartedAtToSet = executionStartedAtToSet,
                executionScheduledAtToSet = executionScheduledAtToSet,
                contextToSet = contextToSet,
                messageToSet = messageToSet,
            )

            // then
            actual.shouldBeRight()
            val updated = transactionAwareDSLContextProxy
                .dslContext()
                .selectFrom(SERVICE_TASK)
                .where(SERVICE_TASK.ID.eq(taskToUpdate.id))
                .awaitSingle()!!
            assertThat(updated.id).isEqualTo(taskToUpdate.id)
            assertThat(updated.type).isEqualTo(taskToUpdate.type)
            assertThat(updated.status).isEqualTo(RecordServiceTaskStatus.COMPLETED)
            assertThat(updated.externalId).isEqualTo(taskToUpdate.externalId)
            assertThat(updated.priority).isEqualTo(taskToUpdate.priority)
            assertThat(updated.executionCount).isEqualTo(1)
            assertThat(updated.executionStartedAt).isNull()
            assertThat(updated.executionScheduledAt).isNull()
            assertThat(updated.context.data()).isEqualTo("{\"output\": {\"status\": \"SUCCESSFUL\"}}")
            assertThat(updated.message).isEqualTo(taskToUpdate.message)
            assertThat(updated.modifiedAt).isEqualTo(OffsetDateTime.parse("2025-01-01T12:00:00Z").toLocalDateTime())
            assertThat(updated.createdAt).isEqualTo(taskToUpdate.createdAt)
        }
    }

    private suspend fun insert(record: ServiceTaskRecord) {
        transactionAwareDSLContextProxy.dslContext()
            .insertInto(SERVICE_TASK)
            .set(record)
            .awaitFirstOrNull()
    }

    private companion object {
        private val objectMapper = jacksonObjectMapper()
            .registerModules(JavaTimeModule())
            .also { it.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) }
    }
}