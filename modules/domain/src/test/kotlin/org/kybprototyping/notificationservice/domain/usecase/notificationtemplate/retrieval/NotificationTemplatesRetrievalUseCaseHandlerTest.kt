package org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.retrieval

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kybprototyping.notificationservice.domain.model.NotificationChannel
import org.kybprototyping.notificationservice.domain.model.NotificationLanguage
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate
import org.kybprototyping.notificationservice.domain.model.NotificationType
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort
import java.time.OffsetDateTime

@ExtendWith(MockKExtension::class)
internal class NotificationTemplatesRetrievalUseCaseHandlerTest {

    @MockK
    private lateinit var notificationTemplateRepositoryPortAdapter: NotificationTemplateRepositoryPort

    @InjectMockKs
    private lateinit var underTest: NotificationTemplatesRetrievalUseCaseHandler

    @Test
    fun `should return investment plans with given values`() = runTest {
        // given
        val input = NotificationTemplatesRetrievalInput(
            channel = NotificationChannel.EMAIL,
            type = NotificationType.WELCOME,
            language = NotificationLanguage.EN
        )
        coEvery { notificationTemplateRepositoryPortAdapter.getListBy(
            NotificationChannel.EMAIL,
            NotificationType.WELCOME,
            NotificationLanguage.EN
        ) } returns listOf(NotificationTemplate(
            id = 1,
            channel = NotificationChannel.EMAIL,
            type = NotificationType.WELCOME,
            language = NotificationLanguage.EN,
            subject = "subject",
            content = "content",
            modificationDate = OffsetDateTime.parse("2024-06-15T09:30:00Z"),
            creationDate = OffsetDateTime.parse("2024-06-15T09:30:00Z")
        ))

        // when
        val actual = underTest.handle(input)

        // then
        assertThat(actual).isEqualTo(listOf(NotificationTemplate(
            id = 1,
            channel = NotificationChannel.EMAIL,
            type = NotificationType.WELCOME,
            language = NotificationLanguage.EN,
            subject = "subject",
            content = "content",
            modificationDate = OffsetDateTime.parse("2024-06-15T09:30:00Z"),
            creationDate = OffsetDateTime.parse("2024-06-15T09:30:00Z")
        )))
        coVerify(exactly = 1) { notificationTemplateRepositoryPortAdapter.getListBy(
            NotificationChannel.EMAIL,
            NotificationType.WELCOME,
            NotificationLanguage.EN
        ) }
    }

}