package org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.retrieval

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.kybprototyping.notificationservice.domain.common.exception.UseCaseException
import org.kybprototyping.notificationservice.domain.model.NotificationChannel
import org.kybprototyping.notificationservice.domain.model.NotificationLanguage
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate
import org.kybprototyping.notificationservice.domain.model.NotificationType
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort
import java.time.OffsetDateTime

@ExtendWith(MockKExtension::class)
internal class NotificationTemplateRetrievalUseCaseHandlerTest {

    @MockK
    private lateinit var notificationTemplateRepositoryPortAdapter: NotificationTemplateRepositoryPort

    @InjectMockKs
    private lateinit var underTest: NotificationTemplateRetrievalUseCaseHandler

    @Test
    fun `should return notification template with given ID`() = runTest {
        // given
        val input = 1
        coEvery { notificationTemplateRepositoryPortAdapter.getById(input) }
            .returns(
                NotificationTemplate(
                    id = 1,
                    channel = NotificationChannel.EMAIL,
                    type = NotificationType.WELCOME,
                    language = NotificationLanguage.EN,
                    content = "content",
                    modificationDate = OffsetDateTime.parse("2024-06-15T09:30:00Z"),
                    creationDate = OffsetDateTime.parse("2024-06-15T09:30:00Z")
                )
            )

        // when
        val actual = underTest.handle(input)

        // then
        assertThat(actual).isEqualTo(NotificationTemplate(
            id = 1,
            channel = NotificationChannel.EMAIL,
            type = NotificationType.WELCOME,
            language = NotificationLanguage.EN,
            content = "content",
            modificationDate = OffsetDateTime.parse("2024-06-15T09:30:00Z"),
            creationDate = OffsetDateTime.parse("2024-06-15T09:30:00Z")
        ))
        coVerify(exactly = 1) { notificationTemplateRepositoryPortAdapter.getById(1) }
    }

    @Test
    fun `should throw non-existent data exception when no notification template exists with given ID`() = runTest {
        // given
        val input = 1
        coEvery { notificationTemplateRepositoryPortAdapter.getById(input) } returns null

        // when
        val actual = assertThrows<UseCaseException> { underTest.handle(input) }

        // then
        Assertions.assertTrue(actual.dueToNonExistentData)
        assertThat(actual.message).isEqualTo("No notification template exists with given ID 1!")
        coVerify(exactly = 1) { notificationTemplateRepositoryPortAdapter.getById(1) }
    }

}