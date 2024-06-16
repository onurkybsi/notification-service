package org.kybprototyping.notificationservice.adapter.rest.notificationtemplate

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import org.junit.jupiter.api.Test
import org.kybprototyping.notificationservice.domain.common.exception.nonExistentData
import org.kybprototyping.notificationservice.domain.common.interfaces.InputOnlyUseCaseHandler
import org.kybprototyping.notificationservice.domain.common.interfaces.InputOutputUseCaseHandler
import org.kybprototyping.notificationservice.domain.model.NotificationChannel
import org.kybprototyping.notificationservice.domain.model.NotificationLanguage
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate
import org.kybprototyping.notificationservice.domain.model.NotificationType
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.creation.NotificationTemplateCreationInput
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.creation.NotificationTemplateCreationOutput
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.retrieval.NotificationTemplatesRetrievalInput
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.update.NotificationTemplateUpdateInput
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.OffsetDateTime

@WebFluxTest(controllers = [NotificationTemplateRestController::class])
@AutoConfigureMockMvc
open class NotificationTemplateRestControllerIntegrationTest {

    @MockkBean
    private lateinit var notificationTemplateCreationUseCaseHandler:
            InputOutputUseCaseHandler<NotificationTemplateCreationInput, NotificationTemplateCreationOutput>
    @MockkBean
    private lateinit var notificationTemplatesRetrievalUseCaseHandler:
            InputOutputUseCaseHandler<NotificationTemplatesRetrievalInput, List<NotificationTemplate>>
    @MockkBean
    private lateinit var notificationTemplateRetrievalUseCaseHandler:
            InputOutputUseCaseHandler<Int, NotificationTemplate>
    @MockkBean
    private lateinit var notificationTemplateUpdateUseCaseHandler:
            InputOutputUseCaseHandler<NotificationTemplateUpdateInput, NotificationTemplate>
    @MockkBean
    private lateinit var notificationTemplateDeletionUseCaseHandler:
            InputOnlyUseCaseHandler<Int>

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `should create a notification template`() {
        // given
        val body = NotificationTemplateCreationInput(
            channel = NotificationChannel.EMAIL,
            type = NotificationType.WELCOME,
            language = NotificationLanguage.EN,
            subject = "subject",
            content = "content"
        )
        coEvery { notificationTemplateCreationUseCaseHandler.handle(body) } returns NotificationTemplateCreationOutput(1)

        // when & then
        webTestClient.post()
            .uri("/api/v1/notification-template")
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isCreated
            .expectHeader()
            .location("/api/v1/notification-template/1")
        coVerify(exactly = 1) { notificationTemplateCreationUseCaseHandler.handle(body) }
    }

    @Test
    fun `should return invalid response when given notification template creation body is not valid`() {
        // given

        // when & then
        webTestClient.post()
            .uri("/api/v1/notification-template")
            .headers { it.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) }
            .bodyValue("""
                {
                    "channel": "EMAIL",
                    "type": "WELCOME",
                    "language": "EN"
                }
                """)
            .exchange()
            .expectStatus()
            .isBadRequest
        coVerify(exactly = 0) { notificationTemplateCreationUseCaseHandler.handle(any()) }
    }

    @Test
    fun `should return notification templates with given values`() {
        // given
        val expectedInput = NotificationTemplatesRetrievalInput(
            channel = NotificationChannel.EMAIL,
            type = NotificationType.WELCOME,
            language = null
        )
        coEvery { notificationTemplatesRetrievalUseCaseHandler.handle(expectedInput) }
            .returns(
                listOf(
                    NotificationTemplate(
                        id = 1,
                        channel = NotificationChannel.EMAIL,
                        type = NotificationType.WELCOME,
                        language = NotificationLanguage.EN,
                        subject = "subject",
                        content = "content",
                        modificationDate = OffsetDateTime.parse("2024-06-15T09:30:00Z"),
                        creationDate = OffsetDateTime.parse("2024-06-15T09:30:00Z")
                    )
                )
            )

        // when & then
        webTestClient.get()
            .uri {
                it.path("/api/v1/notification-template")
                    .queryParam("channel", "EMAIL")
                    .queryParam("type", "WELCOME")
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
                        "subject": "subject",
                        "content": "content",
                        "modificationDate": "2024-06-15T09:30:00Z",
                        "creationDate": "2024-06-15T09:30:00Z"
                      }
                    ]
                """.trimIndent()
            )
        coVerify(exactly = 1) { notificationTemplatesRetrievalUseCaseHandler.handle(expectedInput) }
    }

    @Test
    fun `should return notification template with given ID`() {
        // given
        val id = 1
        coEvery { notificationTemplateRetrievalUseCaseHandler.handle(id) }
            .returns(
                NotificationTemplate(
                    id = 1,
                    channel = NotificationChannel.EMAIL,
                    type = NotificationType.WELCOME,
                    language = NotificationLanguage.EN,
                    subject = "subject",
                    content = "content",
                    modificationDate = OffsetDateTime.parse("2024-06-15T09:30:00Z"),
                    creationDate = OffsetDateTime.parse("2024-06-15T09:30:00Z")
                )
            )

        // when & then
        webTestClient.get()
            .uri("/api/v1/notification-template/{id}", id)
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
                      "subject": "subject",
                      "content": "content",
                      "modificationDate": "2024-06-15T09:30:00Z",
                      "creationDate": "2024-06-15T09:30:00Z"
                    }
                """.trimIndent()
            )
        coVerify(exactly = 1) { notificationTemplateRetrievalUseCaseHandler.handle(id) }
    }

    @Test
    fun `should return not found when no notification template exists with given ID`() {
        // given
        val id = 1
        coEvery { notificationTemplateRetrievalUseCaseHandler.handle(id) }
            .throws(nonExistentData("No notification template exists with given ID 1!"))

        // when & then
        webTestClient.get()
            .uri("/api/v1/notification-template/{id}", id)
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
                      "detail": "No notification template exists with given ID 1!",
                      "instance": "/api/v1/notification-template/1"
                    }
                """.trimIndent()
            )
        coVerify(exactly = 1) { notificationTemplateRetrievalUseCaseHandler.handle(id) }
    }

    @Test
    fun `should update notification template content`() {
        // given
        val id = 1
        val body = NotificationTemplateUpdateRequest(
            subject = "updated subject",
            content = "updated content"
        )
        coEvery { notificationTemplateUpdateUseCaseHandler.handle(
            NotificationTemplateUpdateInput(
                id = id,
                subject = body.subject,
                content = body.content
            )
        ) }.returns(
            NotificationTemplate(
                id = 1,
                channel = NotificationChannel.EMAIL,
                type = NotificationType.WELCOME,
                language = NotificationLanguage.EN,
                subject = "updated subject",
                content = "updated content",
                modificationDate = OffsetDateTime.parse("2024-06-15T09:30:00Z"),
                creationDate = OffsetDateTime.parse("2024-06-15T09:30:00Z")
            )
        )

        // when & then
        webTestClient.patch()
            .uri("/api/v1/notification-template/{id}", id)
            .bodyValue(body)
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
                      "subject": "updated subject",
                      "content": "updated content",
                      "modificationDate": "2024-06-15T09:30:00Z",
                      "creationDate": "2024-06-15T09:30:00Z"
                    }
                """.trimIndent()
            )
        coVerify(exactly = 1) { notificationTemplateUpdateUseCaseHandler.handle(
            NotificationTemplateUpdateInput(
                id = 1,
                subject = "updated subject",
                content = "updated content"
            )
        ) }
    }

    @Test
    fun `should return not found when no notification template exists with given ID to update its content`() {
        // given
        val id = 1
        val body = NotificationTemplateUpdateRequest(
            subject = "updated subject",
            content = "updated content"
        )
        coEvery { notificationTemplateUpdateUseCaseHandler.handle(
            NotificationTemplateUpdateInput(
                id = id,
                subject = body.subject,
                content = body.content
            )
        ) }.throws(nonExistentData("No notification template exists with given ID 1!"))

        // when & then
        webTestClient.patch()
            .uri("/api/v1/notification-template/{id}", id)
            .bodyValue(body)
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
                      "detail": "No notification template exists with given ID 1!",
                      "instance": "/api/v1/notification-template/1"
                    }
                """.trimIndent()
            )
        coVerify(exactly = 1) { notificationTemplateUpdateUseCaseHandler.handle(
            NotificationTemplateUpdateInput(
                id = 1,
                subject = "updated subject",
                content = "updated content"
            )
        ) }
    }

    @Test
    fun `should delete notification template`() {
        // given
        val id = 1
        coJustRun { notificationTemplateDeletionUseCaseHandler.handle(id) }

        // when & then
        webTestClient.delete()
            .uri("/api/v1/notification-template/{id}", id)
            .exchange()
            .expectStatus()
            .isNoContent
            .expectBody()
            .isEmpty
        coVerify(exactly = 1) { notificationTemplateDeletionUseCaseHandler.handle(id) }
    }

    @Test
    fun `should return not found when no notification template exists with given ID to delete`() {
        // given
        val id = 1
        coEvery { notificationTemplateDeletionUseCaseHandler.handle(id) }
            .throws(nonExistentData("No notification template exists with given ID 1!"))

        // when & then
        webTestClient.delete()
            .uri("/api/v1/notification-template/{id}", id)
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
                      "detail": "No notification template exists with given ID 1!",
                      "instance": "/api/v1/notification-template/1"
                    }
                """.trimIndent()
            )
        coVerify(exactly = 1) { notificationTemplateDeletionUseCaseHandler.handle(id) }
    }

}