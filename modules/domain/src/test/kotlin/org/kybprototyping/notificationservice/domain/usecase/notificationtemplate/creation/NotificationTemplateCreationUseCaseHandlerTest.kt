package org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.creation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.assertj.core.api.Assertions.assertThat
import org.kybprototyping.notificationservice.domain.model.NotificationChannel
import org.kybprototyping.notificationservice.domain.model.NotificationLanguage
import org.kybprototyping.notificationservice.domain.model.NotificationType
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort

@ExtendWith(MockKExtension::class)
internal class NotificationTemplateCreationUseCaseHandlerTest {

    @MockK
    private lateinit var notificationTemplateRepositoryPortAdapter: NotificationTemplateRepositoryPort

    @InjectMockKs
    private lateinit var underTest: NotificationTemplateCreationUseCaseHandler

    @Test
    fun `should create a notification template`() = runTest {
        // given
        val input = NotificationTemplateCreationInput(
            channel = NotificationChannel.EMAIL,
            type = NotificationType.WELCOME,
            language = NotificationLanguage.EN,
            subject = "subject",
            content = "content"
        )
        coEvery { notificationTemplateRepositoryPortAdapter.create(input.toNotificationTemplateCreationRequest()) } returns 1

        // when
        val actual = underTest.handle(input)

        // then
        assertThat(actual).isEqualTo(NotificationTemplateCreationOutput(1))
        coVerify { notificationTemplateRepositoryPortAdapter.create(input.toNotificationTemplateCreationRequest()) }
    }

}
