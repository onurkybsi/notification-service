package org.kybprototyping.notificationservice.adapter.rest.notificationtemplate

import arrow.core.left
import arrow.core.right
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import org.junit.jupiter.api.Test
import org.kybprototyping.notificationservice.adapter.rest.notificationtemplate.NotificationChannel.Companion.toDomain
import org.kybprototyping.notificationservice.adapter.rest.notificationtemplate.NotificationLanguage.Companion.toDomain
import org.kybprototyping.notificationservice.adapter.rest.notificationtemplate.NotificationType.Companion.toDomain
import org.kybprototyping.notificationservice.domain.common.DataNotFoundFailure
import org.kybprototyping.notificationservice.domain.common.UseCaseHandler
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplatesRetrievalInput
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.OffsetDateTime
import org.kybprototyping.notificationservice.domain.model.NotificationChannel as DomainNotificationChannel
import org.kybprototyping.notificationservice.domain.model.NotificationLanguage as DomainNotificationLanguage
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate as DomainNotificationTemplate
import org.kybprototyping.notificationservice.domain.model.NotificationType as DomainNotificationType

@WebFluxTest(controllers = [NotificationTemplateRetrievalController::class])
internal class NotificationTemplateRetrievalControllerIntegrationTest {
    @MockkBean
    private lateinit var notificationTemplatesRetrievalUseCase: UseCaseHandler<NotificationTemplatesRetrievalInput, List<DomainNotificationTemplate>>

    @MockkBean
    private lateinit var notificationTemplateRetrievalUseCase: UseCaseHandler<Int, DomainNotificationTemplate>

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `should return notification templates by given filtering values`() {
        // given
        val channel = NotificationChannel.EMAIL
        val type = NotificationType.WELCOME
        val language = NotificationLanguage.EN
        val expectedInput =
            NotificationTemplatesRetrievalInput(
                channel = channel.toDomain(),
                type = type.toDomain(),
                language = language.toDomain(),
            )
        coEvery { notificationTemplatesRetrievalUseCase.handle(expectedInput) } returns listOf(testNotificationTemplate).right()

        // when & then
        webTestClient.get()
            .uri {
                it.path("/api/v1/notification-template")
                    .queryParam("channel", channel.name)
                    .queryParam("type", type.name)
                    .queryParam("language", language.name)
                    .build()
            }
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .json(
                """
                [
                  {
                    "id": 1,
                    "channel": "EMAIL",
                    "type": "WELCOME",
                    "language": "EN",
                    "subject": "Welcome",
                    "content": "Welcome to our platform $\{firstName}",
                    "modifiedAt": "2024-10-01T09:00:00Z",
                    "createdAt": "2024-10-01T09:00:00Z"
                  }
                ]
                """.trimIndent(),
            )
    }

    @Test
    fun `should return internal server error when unexpected exception is thrown during notification templates retrieval use case execution`() {
        // given
        val channel = NotificationChannel.EMAIL
        val type = NotificationType.WELCOME
        val language = NotificationLanguage.EN
        val expectedInput =
            NotificationTemplatesRetrievalInput(
                channel = channel.toDomain(),
                type = type.toDomain(),
                language = language.toDomain(),
            )
        coEvery { notificationTemplatesRetrievalUseCase.handle(expectedInput) } throws RuntimeException("Unexpected exception occurred!")

        // when & then
        webTestClient.get()
            .uri {
                it.path("/api/v1/notification-template")
                    .queryParam("channel", channel.name)
                    .queryParam("type", type.name)
                    .queryParam("language", language.name)
                    .build()
            }
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectBody()
            .json(
                """
                {
                    "type": "about:blank",
                    "title": "Internal Server Error",
                    "status": 500,
                    "detail": "An unexpected error occurred!",
                    "instance": "/api/v1/notification-template"
                }
                """.trimIndent(),
            )
    }

    @Test
    fun `should return notification template by ID`() {
        // given
        val id = 1
        coEvery { notificationTemplateRetrievalUseCase.handle(id) } returns testNotificationTemplate.right()

        // when & then
        webTestClient.get()
            .uri { it.path("/api/v1/notification-template/$id").build() }
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .json(
                """
                {
                    "id": 1,
                    "channel": "EMAIL",
                    "type": "WELCOME",
                    "language": "EN",
                    "subject": "Welcome",
                    "content": "Welcome to our platform ${'$'}\{firstName}",
                    "modifiedAt": "2024-10-01T09:00:00Z",
                    "createdAt": "2024-10-01T09:00:00Z"
                  }
                """.trimIndent(),
            )
    }

    @Test
    fun `should return not found when no notification template found by ID`() {
        // given
        val id = 1
        coEvery { notificationTemplateRetrievalUseCase.handle(id) }
            .returns(DataNotFoundFailure("No notification template found by given ID: 1").left())

        // when & then
        webTestClient.get()
            .uri { it.path("/api/v1/notification-template/$id").build() }
            .exchange()
            .expectStatus()
            .isNotFound
            .expectBody()
            .json(
                """
                {
                    "type": "about:blank",
                    "title": "Not Found",
                    "status": 404,
                    "detail": "No notification template found by given ID: 1",
                    "instance": "/api/v1/notification-template/$id"
                }
                """.trimIndent(),
            )
    }

    @Test
    fun `should return internal server error when unexpected exception is thrown during notification template retrieval use case execution`() {
        // given
        val id = 1
        coEvery { notificationTemplateRetrievalUseCase.handle(id) } throws RuntimeException("Unexpected exception occurred!")

        // when & then
        webTestClient.get()
            .uri { it.path("/api/v1/notification-template/$id").build() }
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectBody()
            .json(
                """
                {
                    "type": "about:blank",
                    "title": "Internal Server Error",
                    "status": 500,
                    "detail": "An unexpected error occurred!",
                    "instance": "/api/v1/notification-template/$id"
                }
                """.trimIndent(),
            )
    }

    private companion object {
        private val testNotificationTemplate =
            DomainNotificationTemplate(
                id = 1,
                channel = DomainNotificationChannel.EMAIL,
                type = DomainNotificationType.WELCOME,
                language = DomainNotificationLanguage.EN,
                subject = "Welcome",
                content = "Welcome to our platform \${firstName}",
                modifiedBy = null,
                modifiedAt = OffsetDateTime.parse("2024-10-01T09:00:00Z"),
                createdBy = null,
                createdAt = OffsetDateTime.parse("2024-10-01T09:00:00Z"),
            )
    }
}
