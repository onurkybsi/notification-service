package org.kybprototyping.notificationservice.domain.scheduled.servicetask

import arrow.atomic.AtomicBoolean
import arrow.core.left
import arrow.core.right
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import org.kybprototying.notificationservice.common.TimeUtils
import org.kybprototying.notificationservice.common.UnexpectedFailure
import org.kybprototyping.notificationservice.domain.TestData
import org.kybprototyping.notificationservice.domain.common.CoroutineDispatcherProvider
import org.kybprototyping.notificationservice.domain.model.ServiceTask
import org.kybprototyping.notificationservice.domain.model.ServiceTaskStatus.*
import org.kybprototyping.notificationservice.domain.model.ServiceTaskType
import org.kybprototyping.notificationservice.domain.port.ServiceTaskRepositoryPort
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
internal class ServiceTaskExecutorJobTest {
    @MockK
    private lateinit var coroutineDispatcherProvider: CoroutineDispatcherProvider

    @MockK
    private lateinit var serviceTaskRepositoryPort: ServiceTaskRepositoryPort

    @MockK
    private lateinit var sendEmailTaskExecutor: ServiceTaskExecutor

    private lateinit var underTest: ServiceTaskExecutorJob

    @BeforeEach
    fun setUp() {
        underTest = ServiceTaskExecutorJob(
            coroutineDispatcherProvider = coroutineDispatcherProvider,
            timeUtils = TimeUtils(Clock.fixed(Instant.ofEpochSecond(1735689600), ZoneId.of("UTC"))),
            serviceTaskRepositoryPort = serviceTaskRepositoryPort,
            executors = mapOf(ServiceTaskType.SEND_EMAIL to sendEmailTaskExecutor),
        )
    }

    @Test
    fun `should execute service tasks`() = runTest {
        // given
        every { coroutineDispatcherProvider.serviceTaskExecutorDispatcher } returns Dispatchers.Unconfined
        val taskToExecute = TestData.serviceTask(
            status = IN_PROGRESS,
            executionStartedAt = OffsetDateTime.parse("2025-01-01T00:00Z"),
            executionScheduledAt = null,
            input = null,
            modifiedAt = OffsetDateTime.parse("2025-01-01T00:00Z")
        )
        coEvery { serviceTaskRepositoryPort.updateBy(any(), any(), any(), any(), any()) } returns listOf(taskToExecute).right()
        coEvery { sendEmailTaskExecutor.execute(any()) } returns Unit.right()

        // when
        underTest.execute()

        // then
        coVerify(exactly = 1) {
            serviceTaskRepositoryPort.updateBy(
                statuses = listOf(PENDING, ERROR),
                executionScheduledAt = OffsetDateTime.parse("2025-01-01T00:00:00Z"),
                statusToSet = IN_PROGRESS,
                executionScheduledAtToSet = null,
                executionStartedAtToSet = OffsetDateTime.parse("2025-01-01T00:00:00Z"),
            )
        }
        coVerify(exactly = 1) { sendEmailTaskExecutor.execute(taskToExecute) }
    }

    @Test
    fun `should execute tasks independently so that no task failure should affect the other tasks executions`() = runTest {
        // given
        every { coroutineDispatcherProvider.serviceTaskExecutorDispatcher } returns Dispatchers.Unconfined
        val task1ToExecute = TestData.serviceTask(
            status = IN_PROGRESS,
            executionStartedAt = OffsetDateTime.parse("2025-01-01T00:00Z"),
            executionScheduledAt = null,
            input = null,
            modifiedAt = OffsetDateTime.parse("2025-01-01T00:00Z")
        )
        val task2ToExecute = TestData.serviceTask(
            status = IN_PROGRESS,
            executionStartedAt = OffsetDateTime.parse("2025-01-01T00:00Z"),
            executionScheduledAt = null,
            input = null,
            modifiedAt = OffsetDateTime.parse("2025-01-01T00:00Z")
        )
        val task3ToExecute = TestData.serviceTask(
            status = IN_PROGRESS,
            executionStartedAt = OffsetDateTime.parse("2025-01-01T00:00Z"),
            executionScheduledAt = null,
            input = null,
            modifiedAt = OffsetDateTime.parse("2025-01-01T00:00Z")
        )
        coEvery { serviceTaskRepositoryPort.updateBy(any(), any(), any(), any(), any()) }
            .returns(listOf(task1ToExecute, task2ToExecute, task3ToExecute).right())
        val taskSlot = mutableListOf<ServiceTask>()
        val task1ExecutionSuccessfullyCompleted = AtomicBoolean(false)
        coEvery { sendEmailTaskExecutor.execute(capture(taskSlot)) }
            .coAnswers {
                val taskExecuting = invocation.args[0] as ServiceTask
                when (taskExecuting) {
                    task1ToExecute -> {
                        delay(500)
                        task1ExecutionSuccessfullyCompleted.set(true)
                        Unit.right()
                    }
                    task2ToExecute -> UnexpectedFailure("Something went wrong!").left()
                    else -> throw RuntimeException("Something went wrong!")
                }
            }

        // when
        assertDoesNotThrow { underTest.execute() }

        // then
        coVerify(exactly = 3) { sendEmailTaskExecutor.execute(any()) }
        assertThat(taskSlot[0]).isEqualTo(task1ToExecute)
        assertThat(taskSlot[1]).isEqualTo(task2ToExecute)
        assertThat(taskSlot[2]).isEqualTo(task3ToExecute)
        assertThat(task1ExecutionSuccessfullyCompleted.get()).isTrue()
    }

    @Test
    fun `should ignore when task status update failed`() = runTest {
        // given
        every { coroutineDispatcherProvider.serviceTaskExecutorDispatcher } returns Dispatchers.Unconfined
        coEvery { serviceTaskRepositoryPort.updateBy(any(), any(), any(), any(), any()) }
            .returns(UnexpectedFailure("Something went wrong!").left())

        // when
        underTest.execute()

        // then
        coVerify(exactly = 1) {
            serviceTaskRepositoryPort.updateBy(
                statuses = listOf(PENDING, ERROR),
                executionScheduledAt = OffsetDateTime.parse("2025-01-01T00:00:00Z"),
                statusToSet = IN_PROGRESS,
                executionScheduledAtToSet = null,
                executionStartedAtToSet = OffsetDateTime.parse("2025-01-01T00:00:00Z"),
            )
        }
        coVerify { sendEmailTaskExecutor wasNot Called }
    }

}