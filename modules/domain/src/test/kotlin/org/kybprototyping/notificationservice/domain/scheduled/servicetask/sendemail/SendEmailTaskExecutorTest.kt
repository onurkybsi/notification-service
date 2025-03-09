package org.kybprototyping.notificationservice.domain.scheduled.servicetask.sendemail

import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.kybprototying.notificationservice.common.TimeUtils
import org.kybprototying.notificationservice.common.UnexpectedFailure
import org.kybprototying.notificationservice.common.UnexpectedFailure.Companion.unexpectedFailure
import org.kybprototyping.notificationservice.domain.TestData
import org.kybprototyping.notificationservice.domain.model.NotificationChannel
import org.kybprototyping.notificationservice.domain.model.NotificationLanguage
import org.kybprototyping.notificationservice.domain.model.NotificationType
import org.kybprototyping.notificationservice.domain.model.ServiceTaskStatus
import org.kybprototyping.notificationservice.domain.port.EmailSenderPort
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort
import org.kybprototyping.notificationservice.domain.port.ServiceTaskRepositoryPort
import org.kybprototyping.notificationservice.domain.scheduled.servicetask.sendemail.SendEmailTaskExecutor.Properties
import org.kybprototyping.notificationservice.domain.scheduled.servicetask.sendemail.SendEmailTaskContext as Context
import org.kybprototyping.notificationservice.domain.scheduled.servicetask.sendemail.SendEmailTaskContext.Failure
import org.kybprototyping.notificationservice.domain.scheduled.servicetask.sendemail.SendEmailTaskContext.FailureType.TEMPLATE_NOT_FOUND
import org.kybprototyping.notificationservice.domain.scheduled.servicetask.sendemail.SendEmailTaskContext.FailureType.EMAIL_SENDER_FAILURE
import org.kybprototyping.notificationservice.domain.scheduled.servicetask.sendemail.SendEmailTaskContext.Output
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.UUID

@ExtendWith(MockKExtension::class)
internal class SendEmailTaskExecutorTest {
    private val properties = Properties("sender@gmail.com", 3, 24, 2)
    private val timeUtils = TimeUtils(Clock.fixed(Instant.parse("2025-01-01T12:00:00Z"), ZoneId.of("UTC")))

    @MockK
    private lateinit var templateRepositoryPort: NotificationTemplateRepositoryPort
    @MockK
    private lateinit var serviceTaskRepositoryPort: ServiceTaskRepositoryPort
    @MockK
    private lateinit var emailSenderPort: EmailSenderPort

    @InjectMockKs
    private lateinit var underTest: SendEmailTaskExecutor

    @Nested
    inner class HappyPath {
        @Test
        fun `should execute send email task given`() = runTest {
            // given
            val input = TestData.sendEmailInput()
            val task = TestData.serviceTask(context = objectMapper.valueToTree(Context(input)))
            mockHappyPath()

            // when
            val actual = underTest.execute(task)

            // then
            actual shouldBeRight Unit
            coVerifyOrder {
                templateRepositoryPort.findOneBy(
                    channel = NotificationChannel.EMAIL,
                    type = NotificationType.WELCOME,
                    language = NotificationLanguage.EN,
                )
                emailSenderPort.send(
                    from = properties.senderAddress,
                    to = input.to,
                    subject = "Welcome",
                    content = "Welcome to our platform Onur",
                )
                serviceTaskRepositoryPort.updateBy(
                    id = task.id,
                    statusToSet = ServiceTaskStatus.COMPLETED,
                    executionCountToSet = 1,
                    executionStartedAtToSet = null,
                    executionScheduledAtToSet = null,
                    contextToSet = objectMapper.valueToTree(Context(input, output = Output(task.externalId, Context.Status.SUCCESSFUL))),
                    messageToSet = null,
                )
            }
        }

        @ParameterizedTest
        @ValueSource(ints = [1, 2])
        fun `should retry for retryable failures`(executionCount: Int) = runTest {
            // given
            val externalId = UUID.randomUUID()
            val input = TestData.sendEmailInput(externalId)
            val failures = listOf(Failure(OffsetDateTime.parse("2025-01-01T12:00:00Z"), EMAIL_SENDER_FAILURE))
            val task = TestData.serviceTask(
                status = ServiceTaskStatus.ERROR,
                externalId = externalId,
                context = objectMapper.valueToTree(Context(input, failures)),
                executionCount = executionCount,
            )
            mockHappyPath()

            // when
            val actual = underTest.execute(task)

            // then
            actual shouldBeRight Unit
            coVerifyOrder {
                templateRepositoryPort.findOneBy(
                    channel = NotificationChannel.EMAIL,
                    type = NotificationType.WELCOME,
                    language = NotificationLanguage.EN,
                )
                emailSenderPort.send(
                    from = properties.senderAddress,
                    to = input.to,
                    subject = "Welcome",
                    content = "Welcome to our platform Onur",
                )
                serviceTaskRepositoryPort.updateBy(
                    id = task.id,
                    statusToSet = ServiceTaskStatus.COMPLETED,
                    executionCountToSet = executionCount + 1,
                    executionStartedAtToSet = null,
                    executionScheduledAtToSet = null,
                    contextToSet = objectMapper.valueToTree(Context(input, failures, Output(task.externalId, Context.Status.SUCCESSFUL))),
                    messageToSet = null,
                )
            }
        }
    }

    @Nested
    inner class Failure {
        @Test
        fun `should fail unexpectedly when context extraction failed`() = runTest {
            // given
            val task = TestData.serviceTask(context = objectMapper.createObjectNode())

            // when
            val actual = underTest.execute(task)

            // then
            actual.shouldBeLeft().also { assertThat(it).isInstanceOf(UnexpectedFailure::class.java) }
            coVerify {
                listOf(
                    templateRepositoryPort,
                    emailSenderPort,
                    serviceTaskRepositoryPort,
                ) wasNot Called
            }
        }

        @Test
        fun `should fail gracefully when template could not be fetched`() = runTest {
            // given
            val input = TestData.sendEmailInput()
            val task = TestData.serviceTask(context = objectMapper.valueToTree(Context(input)))
            mockHappyPath()
            coEvery { templateRepositoryPort.findOneBy(any(), any(), any()) } returns unexpectedFailure.left()

            // when
            val actual = underTest.execute(task)

            // then
            actual shouldBeLeft unexpectedFailure
            val expectedContext = Context(input, listOf(Failure(OffsetDateTime.parse("2025-01-01T12:00:00Z"), TEMPLATE_NOT_FOUND)))
            coVerifyOrder {
                templateRepositoryPort.findOneBy(
                    channel = NotificationChannel.EMAIL,
                    type = NotificationType.WELCOME,
                    language = NotificationLanguage.EN,
                )
                serviceTaskRepositoryPort.updateBy(
                    id = task.id,
                    statusToSet = ServiceTaskStatus.ERROR,
                    executionCountToSet = 1,
                    executionStartedAtToSet = null,
                    executionScheduledAtToSet = OffsetDateTime.parse("2025-01-02T12:00:00Z"),
                    contextToSet = objectMapper.valueToTree(expectedContext),
                    messageToSet = null,
                )
            }
            coVerify { emailSenderPort wasNot Called }
        }

        @Test
        fun `should fail gracefully when email sender failed`() = runTest {
            // given
            val input = TestData.sendEmailInput()
            val task = TestData.serviceTask(context = objectMapper.valueToTree(Context(input)))
            mockHappyPath()
            coEvery { emailSenderPort.send(any(), any(), any(), any()) } returns unexpectedFailure.left()

            // when
            val actual = underTest.execute(task)

            // then
            actual shouldBeLeft unexpectedFailure
            val expectedContext = Context(input, listOf(Failure(OffsetDateTime.parse("2025-01-01T12:00:00Z"), EMAIL_SENDER_FAILURE)))
            coVerifyOrder {
                templateRepositoryPort.findOneBy(
                    channel = NotificationChannel.EMAIL,
                    type = NotificationType.WELCOME,
                    language = NotificationLanguage.EN,
                )
                emailSenderPort.send(
                    from = properties.senderAddress,
                    to = input.to,
                    subject = "Welcome",
                    content = "Welcome to our platform Onur",
                )
                serviceTaskRepositoryPort.updateBy(
                    id = task.id,
                    statusToSet = ServiceTaskStatus.ERROR,
                    executionCountToSet = 1,
                    executionStartedAtToSet = null,
                    executionScheduledAtToSet = OffsetDateTime.parse("2025-01-01T12:02:00Z"),
                    contextToSet = objectMapper.valueToTree(expectedContext),
                    messageToSet = null,
                )
            }
        }

        @Test
        fun `should fail gracefully when max execution count reached`() = runTest {
            // given
            val input = TestData.sendEmailInput()
            val task = TestData.serviceTask(executionCount = 2, context = objectMapper.valueToTree(Context(input)))
            mockHappyPath()
            coEvery { emailSenderPort.send(any(), any(), any(), any()) } returns unexpectedFailure.left()

            // when
            val actual = underTest.execute(task)

            // then
            actual shouldBeLeft unexpectedFailure
            val expectedContext = Context(
                input = input,
                failures = listOf(Failure(OffsetDateTime.parse("2025-01-01T12:00:00Z"), EMAIL_SENDER_FAILURE)),
                output = Output(task.externalId, Context.Status.FAILED, EMAIL_SENDER_FAILURE),
            )
            coVerifyOrder {
                templateRepositoryPort.findOneBy(
                    channel = NotificationChannel.EMAIL,
                    type = NotificationType.WELCOME,
                    language = NotificationLanguage.EN,
                )
                emailSenderPort.send(
                    from = properties.senderAddress,
                    to = input.to,
                    subject = "Welcome",
                    content = "Welcome to our platform Onur",
                )
                serviceTaskRepositoryPort.updateBy(
                    id = task.id,
                    statusToSet = ServiceTaskStatus.FAILED,
                    executionCountToSet = 3,
                    executionStartedAtToSet = null,
                    executionScheduledAtToSet = null,
                    contextToSet = objectMapper.valueToTree(expectedContext),
                    messageToSet = "Max execution count 3 reached!",
                )
            }
        }
    }

    private fun mockHappyPath() {
        coEvery { templateRepositoryPort.findOneBy(any(), any(), any()) } returns TestData.notificationTemplate.right()
        coEvery { emailSenderPort.send(any(), any(), any(), any()) } returns Unit.right()
        coEvery { serviceTaskRepositoryPort.updateBy(any(), any(), any(), any(), any(), any(), any()) } returns Unit.right()
    }

    private companion object {
        private val objectMapper = jacksonObjectMapper()
            .registerModules(JavaTimeModule())
            .also { it.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) }
    }
}