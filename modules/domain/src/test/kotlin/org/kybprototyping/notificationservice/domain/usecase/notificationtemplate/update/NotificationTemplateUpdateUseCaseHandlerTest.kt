package org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.update

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.kybprototyping.notificationservice.domain.common.exception.UseCaseException
import org.kybprototyping.notificationservice.domain.model.*
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort
import java.time.OffsetDateTime

@ExtendWith(MockKExtension::class)
internal class NotificationTemplateUpdateUseCaseHandlerTest {

    @MockK
    private lateinit var notificationTemplateRepositoryPortAdapter: NotificationTemplateRepositoryPort

    @InjectMockKs
    private lateinit var underTest: NotificationTemplateUpdateUseCaseHandler

    @Test
    fun `should update notification template and return updated`() = runTest {
        // given
        val input = NotificationTemplateUpdateInput(
            id = 1,
            subject = "updated subject",
            content = "updated content"
        )
        coEvery { notificationTemplateRepositoryPortAdapter.updateBy(NotificationTemplateUpdateRequest(
            id = 1,
            subject = "updated subject",
            content = "updated content"
        )) } returns NotificationTemplate(
            id = 1,
            channel = NotificationChannel.EMAIL,
            type = NotificationType.WELCOME,
            language = NotificationLanguage.EN,
            subject = "updated subject",
            content = "updated content",
            modificationDate = OffsetDateTime.parse("2024-06-16T09:30:00Z"),
            creationDate = OffsetDateTime.parse("2024-06-15T09:30:00Z")
        )

        // when
        val actual = underTest.handle(input)

        // then
        assertThat(actual).isEqualTo(NotificationTemplate(
            id = 1,
            channel = NotificationChannel.EMAIL,
            type = NotificationType.WELCOME,
            language = NotificationLanguage.EN,
            subject = "updated subject",
            content = "updated content",
            modificationDate = OffsetDateTime.parse("2024-06-16T09:30:00Z"),
            creationDate = OffsetDateTime.parse("2024-06-15T09:30:00Z")
        ))
        coVerify(exactly = 1) { notificationTemplateRepositoryPortAdapter.updateBy(NotificationTemplateUpdateRequest(
            id = 1,
            subject = "updated subject",
            content = "updated content"
        )) }
    }

    @Test
    fun `should return null when given input have no value to update`() = runTest {
        // given
        val input = NotificationTemplateUpdateInput(1, null, null)

        // when
        val actual = underTest.handle(input)

        // then
        assertThat(actual).isEqualTo(null)
        coVerify(exactly = 0) { notificationTemplateRepositoryPortAdapter.updateBy(any()) }
    }

    @Test
    fun `should throw nonExistentData exception when no notification template exists with given ID`() = runTest {
        // given
        val input = NotificationTemplateUpdateInput(
            id = 1,
            subject = "updated subject",
            content = "updated content"
        )
        coEvery { notificationTemplateRepositoryPortAdapter.updateBy(NotificationTemplateUpdateRequest(
            id = 1,
            subject = "updated subject",
            content = "updated content"
        )) } returns null

        // when
        val actual = assertThrows<UseCaseException> { underTest.handle(input)  }

        // then
        assertThat(actual.dueToNonExistentData).isEqualTo(true)
        assertThat(actual.message).isEqualTo("No notification template exists with given ID 1!")
        coVerify(exactly = 1) { notificationTemplateRepositoryPortAdapter.updateBy(NotificationTemplateUpdateRequest(
            id = 1,
            subject = "updated subject",
            content = "updated content"
        )) }
    }

}