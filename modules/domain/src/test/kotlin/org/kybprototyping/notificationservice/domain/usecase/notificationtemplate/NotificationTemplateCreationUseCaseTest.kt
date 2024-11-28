package org.kybprototyping.notificationservice.domain.usecase.notificationtemplate

import arrow.core.left
import arrow.core.right
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kybprototyping.notificationservice.domain.common.DataConflictFailure
import org.kybprototyping.notificationservice.domain.common.UnexpectedFailure
import org.kybprototyping.notificationservice.domain.model.NotificationChannel
import org.kybprototyping.notificationservice.domain.model.NotificationLanguage
import org.kybprototyping.notificationservice.domain.model.NotificationType
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort

@ExtendWith(MockKExtension::class)
internal class NotificationTemplateCreationUseCaseTest {
    @MockK
    private lateinit var repositoryPort: NotificationTemplateRepositoryPort

    @InjectMockKs
    private lateinit var underTest: NotificationTemplateCreationUseCase

    @Test
    fun `should create a notification template`() =
        runTest {
            // given
            coEvery {
                repositoryPort.create(
                    testInput.channel, testInput.type,
                    testInput.language, testInput.subject, testInput.content,
                )
            } returns 1.right()

            // when
            val actual = underTest.handle(testInput)

            // then
            actual shouldBeRight 1
        }

    @Test
    fun `should return DataConflictFailure when notification template with given channel, type and language is already created`() =
        runTest {
            // given
            coEvery {
                repositoryPort.create(
                    testInput.channel, testInput.type,
                    testInput.language, testInput.subject, testInput.content,
                )
            } returns null.right()

            // when
            val actual = underTest.handle(testInput)

            // then
            actual shouldBeLeft DataConflictFailure("Template with given EMAIL, WELCOME and EN is already created!")
        }

    @Test
    fun `should return UnexpectedFailure when something went unexpectedly wrong during execution`() =
        runTest {
            // given
            coEvery {
                repositoryPort.create(
                    testInput.channel, testInput.type,
                    testInput.language, testInput.subject, testInput.content,
                )
            } returns UnexpectedFailure("Something went unexpectedly wrong!").left()

            // when
            val actual = underTest.handle(testInput)

            // then
            actual shouldBeLeft UnexpectedFailure(isTemporary = true)
        }

    private companion object {
        val testInput =
            NotificationTemplateCreationInput(
                channel = NotificationChannel.EMAIL,
                type = NotificationType.WELCOME,
                language = NotificationLanguage.EN,
                subject = "Welcome",
                content = "Welcome to our platform \${firstName}!",
            )
    }
}
