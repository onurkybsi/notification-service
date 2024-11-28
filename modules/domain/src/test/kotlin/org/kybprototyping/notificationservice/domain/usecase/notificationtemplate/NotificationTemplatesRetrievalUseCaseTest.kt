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
import org.kybprototyping.notificationservice.domain.TestData
import org.kybprototyping.notificationservice.domain.common.UnexpectedFailure
import org.kybprototyping.notificationservice.domain.model.NotificationChannel
import org.kybprototyping.notificationservice.domain.model.NotificationLanguage
import org.kybprototyping.notificationservice.domain.model.NotificationType
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort

@ExtendWith(MockKExtension::class)
internal class NotificationTemplatesRetrievalUseCaseTest {
    @MockK
    private lateinit var repositoryPort: NotificationTemplateRepositoryPort

    @InjectMockKs
    private lateinit var underTest: NotificationTemplatesRetrievalUseCase

    @Test
    fun `should return notification templates with given filtering values`() =
        runTest {
            // given
            val channel = NotificationChannel.EMAIL
            val type = NotificationType.WELCOME
            val language = NotificationLanguage.EN
            val input =
                NotificationTemplatesRetrievalInput(
                    channel = channel,
                    type = type,
                    language = language,
                )
            coEvery { repositoryPort.findBy(channel, type, language) } returns listOf(TestData.notificationTemplate).right()

            // when
            val actual = underTest.handle(input)

            // then
            actual shouldBeRight listOf(TestData.notificationTemplate)
        }

    @Test
    fun `should return UnexpectedFailure when something went unexpectedly wrong during execution`() =
        runTest {
            // given
            val channel = NotificationChannel.EMAIL
            val type = NotificationType.WELCOME
            val language = NotificationLanguage.EN
            val input =
                NotificationTemplatesRetrievalInput(
                    channel = channel,
                    type = type,
                    language = language,
                )
            coEvery { repositoryPort.findBy(channel, type, language) } returns UnexpectedFailure("Something went unexpectedly wrong!").left()

            // when
            val actual = underTest.handle(input)

            // then
            actual shouldBeLeft UnexpectedFailure(isTemporary = true)
        }
}
