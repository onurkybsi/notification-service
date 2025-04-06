package org.kybprototyping.notificationservice.domain.scheduled.servicetask

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.coEvery
import io.mockk.coVerifyCount
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import nl.altindag.log.LogCaptor
import org.apache.commons.lang3.stream.IntStreams.range
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.kybprototying.notificationservice.common.Failure
import org.kybprototying.notificationservice.common.UnexpectedFailure.Companion.unexpectedFailure
import org.kybprototyping.notificationservice.domain.TestData
import org.kybprototyping.notificationservice.domain.model.ServiceTask
import org.kybprototyping.notificationservice.domain.model.ServiceTaskStatus
import org.kybprototyping.notificationservice.domain.model.ServiceTaskStatus.COMPLETED
import org.kybprototyping.notificationservice.domain.model.ServiceTaskStatus.FAILED
import org.kybprototyping.notificationservice.domain.model.ServiceTaskStatus.PUBLISHED
import org.kybprototyping.notificationservice.domain.model.ServiceTaskType.SEND_EMAIL
import org.kybprototyping.notificationservice.domain.port.ServiceTaskPublisherPort
import org.kybprototyping.notificationservice.domain.port.ServiceTaskRepositoryPort
import org.kybprototyping.notificationservice.domain.port.TransactionalExecutor
import org.kybprototyping.notificationservice.domain.scheduled.servicetask.sendemail.SendEmailTaskContext
import java.util.UUID

@ExtendWith(MockKExtension::class)
internal class ServiceTaskOutputPublisherJobTest {
    @MockK
    private lateinit var transactionalExecutor: TransactionalExecutor

    @MockK
    private lateinit var serviceTaskRepositoryPort: ServiceTaskRepositoryPort

    @MockK
    private lateinit var serviceTaskPublisherPort: ServiceTaskPublisherPort

    @AfterEach
    fun cleanUp() { logCaptor!!.clearLogs() }

    @InjectMockKs
    private lateinit var underTest: ServiceTaskOutputPublisherJob

    @Nested
    inner class HappyPath {
        @Test
        fun `should publish completed task batch`() = runTest {
            // given
            mockHappyPath()

            // when
            underTest.execute()

            // then
            val externalIdsCaptured = mutableListOf<UUID>()
            val outputsCaptured = mutableListOf<ByteArray>()
            val taskIdsCaptured = mutableListOf<UUID>()
            val taskStatusesCaptured = mutableListOf<ServiceTaskStatus>()
            coVerifyCount {
                3 * { transactionalExecutor.execute<ServiceTask?>(any()) }
                3 * { serviceTaskRepositoryPort.lockBy(listOf(SEND_EMAIL), listOf(COMPLETED, FAILED)) }
                2 * { serviceTaskPublisherPort.execute(eq(SEND_EMAIL), capture(externalIdsCaptured), capture(outputsCaptured)) }
                2 * { serviceTaskRepositoryPort.updateBy(capture(taskIdsCaptured), capture(taskStatusesCaptured)) }
            }
            assertThat(externalIdsCaptured).isEqualTo(listOf(firstExpectedTask.externalId, secondExpectedTask.externalId))
            val firstExpectedOutput =
                objectMapper
                    .writeValueAsBytes(
                        objectMapper
                            .readValue(
                                "{\"id\":\"11a2c3b0-9559-4f8e-a7dc-e6dfebdb32ee\",\"status\":\"SUCCESSFUL\",\"failureType\":null}",
                                SendEmailTaskContext.Output::class.java,
                            )
                    )
            assertThat(outputsCaptured[0]).isEqualTo(firstExpectedOutput)
            val secondExpectedOutput =
                objectMapper
                    .writeValueAsBytes(
                        objectMapper
                            .readValue(
                                "{\"id\":\"11a2c3b0-9559-4f8e-a7dc-e6dfebdb32ee\",\"status\":\"FAILED\",\"failureType\":\"TEMPLATE_NOT_FOUND\"}",
                                SendEmailTaskContext.Output::class.java,
                            )
                    )
            assertThat(outputsCaptured[1]).isEqualTo(secondExpectedOutput)
            assertThat(taskIdsCaptured).isEqualTo(listOf(firstExpectedTask.id, secondExpectedTask.id))
            assertThat(taskStatusesCaptured).isEqualTo(listOf(PUBLISHED, PUBLISHED))
            assertThat(logCaptor!!.infoLogs)
                .isEqualTo(
                    listOf(
                        "Task publisher job is being started...",
                        "Task publisher job completed: numOfFailures: 0, numOfSuccess: 2",
                    )
                )
        }
    }

    @Nested
    inner class FailurePaths {
        @Test
        fun `should fail when publishable task lock failed`() = runTest {
            // given
            mockHappyPath()
            coEvery { serviceTaskRepositoryPort.lockBy(any(), any()) } returns unexpectedFailure.left()

            // when
            underTest.execute()

            // then
            coVerifyCount {
                10 * { transactionalExecutor.execute<ServiceTask?>(any()) }
                10 * { serviceTaskRepositoryPort.lockBy(listOf(SEND_EMAIL), listOf(COMPLETED, FAILED)) }
                0 * { serviceTaskPublisherPort.execute(any(), any(), any()) }
                0 * { serviceTaskRepositoryPort.updateBy(any(), any()) }
            }
            assertThat(logCaptor!!.infoLogs)
                .isEqualTo(
                    listOf(
                        "Task publisher job is being started...",
                        "Task publisher job completed: numOfFailures: 10, numOfSuccess: 0",
                    )
                )
            assertThat(logCaptor!!.warnLogs)
                .isEqualTo(
                    range(10)
                        .mapToObj { "No task could be locked: $unexpectedFailure" }
                        .toList()
                )
        }

        @Test
        fun `should fail when output deserialization failed`() = runTest {
            // given
            mockHappyPath()
            val invalidCtx =
                objectMapper
                    .readTree(
                        """
                            {
                                "input": {
                                    "type": "WELCOME",
                                    "language": "EN",
                                    "to": "recipient2@gmail.com",
                                    "values": { "firstName": "FirstName2" },
                                    "externalId": "11a2c3b0-9559-4f8e-a7dc-e6dfebdb32ee"
                                 },
                                "output": {}
                            }
                        """.trimIndent()
                    )
            coEvery { serviceTaskRepositoryPort.lockBy(any(), any()) }
                .returnsMany(listOf(firstExpectedTask.copy(context = invalidCtx).right(), null.right()))

            // when
            underTest.execute()

            // then
            coVerifyCount {
                2 * { transactionalExecutor.execute<ServiceTask?>(any()) }
                2 * { serviceTaskRepositoryPort.lockBy(listOf(SEND_EMAIL), listOf(COMPLETED, FAILED)) }
                0 * { serviceTaskPublisherPort.execute(any(), any(), any()) }
                0 * { serviceTaskRepositoryPort.updateBy(any(), any()) }
            }
            assertThat(logCaptor!!.infoLogs)
                .isEqualTo(
                    listOf(
                        "Task publisher job is being started...",
                        "Task publisher job completed: numOfFailures: 1, numOfSuccess: 0",
                    )
                )
            assertThat(logCaptor!!.warnLogs)
                .isEqualTo(listOf("Task serialization failed: ${firstExpectedTask.id}"))
        }

        @Test
        fun `should continue to execute when a particular publishable task failed`() = runTest {
            // given
            mockHappyPath()
            coEvery { serviceTaskRepositoryPort.lockBy(any(), any()) }
                .returnsMany(listOf(firstExpectedTask.right(), secondExpectedTask.right(), null.right()))
            coEvery { serviceTaskPublisherPort.execute(any(), eq(firstExpectedTask.externalId), any()) }
                .returns(unexpectedFailure.left())

            // when
            underTest.execute()

            // then
            coVerifyCount {
                3 * { transactionalExecutor.execute<ServiceTask?>(any()) }
                3 * { serviceTaskRepositoryPort.lockBy(listOf(SEND_EMAIL), listOf(COMPLETED, FAILED)) }
                2 * { serviceTaskPublisherPort.execute(any(), any(), any()) }
                1 * { serviceTaskRepositoryPort.updateBy(any(), any()) }
            }
            assertThat(logCaptor!!.infoLogs)
                .isEqualTo(
                    listOf(
                        "Task publisher job is being started...",
                        "Task publisher job completed: numOfFailures: 1, numOfSuccess: 1",
                    )
                )
            assertThat(logCaptor!!.warnLogs)
                .isEqualTo(listOf("${firstExpectedTask.id} couldn't be published: $unexpectedFailure"))
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun mockHappyPath() {
        coEvery { transactionalExecutor.execute<ServiceTask?>(any()) }
            .coAnswers { (it.invocation.args[0] as suspend () -> Either<Failure, ServiceTask?>).invoke() }
        coEvery { serviceTaskRepositoryPort.lockBy(any(), any()) }
            .returnsMany(listOf(firstExpectedTask.right(), secondExpectedTask.right(), null.right()))
        coEvery { serviceTaskPublisherPort.execute(any(), any(), any()) } returns Unit.right()
        coEvery { serviceTaskRepositoryPort.updateBy(any(), any()) } returns Unit.right()
    }

    private companion object {
        private val objectMapper = jacksonObjectMapper()
            .registerModules(JavaTimeModule())
            .also { it.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) }
        private val firstExpectedTask = TestData
            .serviceTask(
                externalId = UUID.fromString("11a2c3b0-9559-4f8e-a7dc-e6dfebdb32ee"),
                status = COMPLETED,
                context = objectMapper
                    .readTree(
                        """
                            {
                                "input": {
                                    "type": "WELCOME",
                                    "language": "EN",
                                    "to": "recipient2@gmail.com",
                                    "values": { "firstName": "FirstName2" },
                                    "externalId": "11a2c3b0-9559-4f8e-a7dc-e6dfebdb32ee"
                                 },
                                "output": { 
                                    "id": "11a2c3b0-9559-4f8e-a7dc-e6dfebdb32ee",
                                    "status": "SUCCESSFUL"
                                }
                            }
                        """.trimIndent()
                    )
            )
        private val secondExpectedTask = TestData
            .serviceTask(
                externalId = UUID.fromString("8621b804-71a1-4ec4-b1a6-252ff615f7e7"),
                status = FAILED,
                context = objectMapper
                    .readTree(
                        """
                            {
                                "input": {
                                    "type": "WELCOME",
                                    "language": "EN",
                                    "to": "recipient1@gmail.com",
                                    "values": { "firstName": "FirstName1" },
                                    "externalId": "8621b804-71a1-4ec4-b1a6-252ff615f7e7"
                                 },
                                "output": { 
                                    "id": "11a2c3b0-9559-4f8e-a7dc-e6dfebdb32ee",
                                    "status": "FAILED",
                                    "failureType": "TEMPLATE_NOT_FOUND"
                                }
                            }
                        """.trimIndent()
                    )
            )
        private var logCaptor: LogCaptor? = null

        @JvmStatic
        @BeforeAll
        fun setUpBeforeAll() { logCaptor = LogCaptor.forClass(ServiceTaskOutputPublisherJob::class.java) }

        @JvmStatic
        @AfterAll
        fun cleanUpAfterAll() { logCaptor!!.close() }
    }
}