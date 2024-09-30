package org.kybprototyping.notificationservice.domain.usecase.notificationtemplate

import arrow.core.left
import arrow.core.right
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kybprototyping.notificationservice.domain.common.DataNotFoundFailure
import org.kybprototyping.notificationservice.domain.common.UnexpectedFailure
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort

@ExtendWith(MockKExtension::class)
internal class NotificationTemplateUpdateUseCaseTest {

    @MockK
    private lateinit var repositoryPort: NotificationTemplateRepositoryPort

    @InjectMockKs
    private lateinit var underTest: NotificationTemplateUpdateUseCase

    @Test
    fun `should update notification template by given input`() = runTest {
        // given
        val input = NotificationTemplateUpdateInput(
            id = 1,
            subject = "Updated Content",
            content = "Updated Content"
        )
        coEvery { repositoryPort.update(any(), any(), any()) } returns Unit.right()

        // when
        val actual = underTest.handle(input)

        // then
        actual shouldBeRight Unit
        coVerify(exactly = 1) { repositoryPort.update(id = input.id, subjectToSet = input.subject, contentToSet = input.content) }
    }

    @Test
    fun `should return immediately when subject and content given as null`() = runTest {
        // given
        val input = NotificationTemplateUpdateInput(
            id = 1,
            subject = null,
            content = null
        )
        coEvery { repositoryPort.update(any(), any(), any()) } returns Unit.right()

        // when
        val actual = underTest.handle(input)

        // then
        actual shouldBeRight Unit
        coVerify { repositoryPort wasNot Called }
    }

    @Test
    fun `should return DataNotFoundFailure when the template to updated doesn't exist by given ID`() = runTest {
        // given
        val input = NotificationTemplateUpdateInput(
            id = 1,
            subject = "Updated Content",
            content = "Updated Content"
        )
        coEvery { repositoryPort.update(any(), any(), any()) } returns NotificationTemplateRepositoryPort.UpdateFailure.DataNotFoundFailure.left()

        // when
        val actual = underTest.handle(input)

        // then
        actual shouldBeLeft DataNotFoundFailure("No notification template found to update by given ID: 1")
        coVerify(exactly = 1) { repositoryPort.update(id = input.id, subjectToSet = input.subject, contentToSet = input.content) }
    }

    @Test
    fun `should return UnexpectedFailure when something went unexpectedly wrong during execution`() = runTest {
        // given
        val input = NotificationTemplateUpdateInput(
            id = 1,
            subject = "Updated Content",
            content = "Updated Content"
        )
        coEvery { repositoryPort.update(any(), any(), any()) }
            .returns(NotificationTemplateRepositoryPort.UpdateFailure.UnexpectedFailure(RuntimeException("Something went unexpectedly wrong!")).left())

        // when
        val actual = underTest.handle(input)

        // then
        actual shouldBeLeft UnexpectedFailure(isTemporary = true)
        coVerify(exactly = 1) { repositoryPort.update(id = input.id, subjectToSet = input.subject, contentToSet = input.content) }
    }

}