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
import org.kybprototying.notificationservice.common.DataNotFoundFailure
import org.kybprototying.notificationservice.common.UnexpectedFailure
import org.kybprototyping.notificationservice.domain.TestData
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort

@ExtendWith(MockKExtension::class)
internal class NotificationTemplateDeletionUseCaseTest {
    @MockK
    private lateinit var repositoryPort: NotificationTemplateRepositoryPort

    @InjectMockKs
    private lateinit var underTest: NotificationTemplateDeletionUseCase

    @Test
    fun `should delete an existing template by given ID`() =
        runTest {
            // given
            val id = TestData.notificationTemplate.id
            coEvery { repositoryPort.delete(id) } returns Unit.right()

            // when
            val actual = underTest.handle(id)

            // then
            actual shouldBeRight Unit
        }

    @Test
    fun `should return DataNotFoundFailure when the template to delete doesn't exist by given ID`() =
        runTest {
            // given
            val id = TestData.notificationTemplate.id
            coEvery { repositoryPort.delete(id) } returns NotificationTemplateRepositoryPort.DeletionFailure.DataNotFoundFailure.left()

            // when
            val actual = underTest.handle(id)

            // then
            actual shouldBeLeft DataNotFoundFailure("No notification template found to delete by given ID: 1")
        }

    @Test
    fun `should return UnexpectedFailure when something went unexpectedly wrong during execution`() =
        runTest {
            // given
            val id = TestData.notificationTemplate.id
            coEvery { repositoryPort.delete(id) }
                .returns(
                    NotificationTemplateRepositoryPort.DeletionFailure.UnexpectedFailure(
                        RuntimeException("Something went unexpectedly wrong!"),
                    ).left(),
                )

            // when
            val actual = underTest.handle(id)

            // then
            actual shouldBeLeft UnexpectedFailure(isTemporary = true)
        }
}
