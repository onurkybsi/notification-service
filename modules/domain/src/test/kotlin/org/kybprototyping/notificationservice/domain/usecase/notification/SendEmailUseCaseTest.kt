package org.kybprototyping.notificationservice.domain.usecase.notification

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kybprototying.notificationservice.common.DataConflictFailure
import org.kybprototying.notificationservice.common.Failure
import org.kybprototying.notificationservice.common.TimeUtils
import org.kybprototying.notificationservice.common.UnexpectedFailure
import org.kybprototying.notificationservice.common.UnexpectedFailure.Companion.unexpectedFailure
import org.kybprototyping.notificationservice.domain.model.*
import org.kybprototyping.notificationservice.domain.port.ServiceTaskRepositoryPort
import org.kybprototyping.notificationservice.domain.scheduled.servicetask.sendemail.SendEmailTaskContext
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.UUID

@ExtendWith(MockKExtension::class)
internal class SendEmailUseCaseTest {
    private val timeUtils: TimeUtils = TimeUtils(Clock.fixed(Instant.ofEpochSecond(1735689600), ZoneId.of("UTC")))

    @MockK
    private lateinit var serviceTaskRepositoryPort: ServiceTaskRepositoryPort

    private lateinit var underTest: SendEmailUseCase

    @BeforeEach
    fun setUp() {
        underTest = SendEmailUseCase(timeUtils, serviceTaskRepositoryPort)
    }

    @Test
    fun `should insert send email task to execute`() = runTest {
        // given
        val input = SendEmailInput(
            type = NotificationType.WELCOME,
            language = NotificationLanguage.EN,
            to = "recipient@gmail.com",
            values = mapOf("firstName" to "Onur"),
            externalId = UUID.randomUUID(),
        )
        coEvery { serviceTaskRepositoryPort.insert(any()) } returns Unit.right()

        // when
        var actual: Either<Failure, SendEmailOutput>? = null
        val expectedId = UUID.randomUUID()
        mockkStatic(UUID::class) {
            every { UUID.randomUUID() } returns expectedId
            actual = underTest.handle(input)
        }

        // then
        actual!! shouldBeRight SendEmailOutput(input.externalId!!)
        coVerify(exactly = 1) {
            serviceTaskRepositoryPort.insert(
                ServiceTask(
                    id = expectedId,
                    type = ServiceTaskType.SEND_EMAIL,
                    status = ServiceTaskStatus.PENDING,
                    externalId = input.externalId!!,
                    priority = ServiceTaskPriority.MEDIUM,
                    executionCount = 0,
                    executionStartedAt = null,
                    executionScheduledAt = null,
                    context = objectMapper.valueToTree(SendEmailTaskContext(input)),
                    message = null,
                    modifiedAt = OffsetDateTime.parse("2025-01-01T00:00:00Z"),
                    createdAt = OffsetDateTime.parse("2025-01-01T00:00:00Z"),
                )
            )
        }
    }

    @Test
    fun `should fail with DataConflictFailure when there is already a send email task with given external ID`() = runTest {
        // given
        val input = SendEmailInput(
            type = NotificationType.WELCOME,
            language = NotificationLanguage.EN,
            to = "recipient@gmail.com",
            values = mapOf("firstName" to "Onur"),
            externalId = UUID.randomUUID(),
        )
        coEvery { serviceTaskRepositoryPort.insert(any()) } returns DataConflictFailure("Conflict!").left()

        // when
        val actual = underTest.handle(input)

        // then
        actual shouldBeLeft DataConflictFailure("There is already a send email task created with given external ID: ${input.externalId}")
    }

    @Test
    fun `should fail with UnexpectedFailure when task insertion failed unexpectedly`() = runTest {
        // given
        val input = SendEmailInput(
            type = NotificationType.WELCOME,
            language = NotificationLanguage.EN,
            to = "recipient@gmail.com",
            values = mapOf("firstName" to "Onur"),
            externalId = UUID.randomUUID(),
        )
        coEvery { serviceTaskRepositoryPort.insert(any()) } returns UnexpectedFailure("Something went wrong!").left()

        // when
        val actual = underTest.handle(input)

        // then
        actual shouldBeLeft unexpectedFailure
    }

    private companion object {
        private val objectMapper = jacksonObjectMapper()
    }
}