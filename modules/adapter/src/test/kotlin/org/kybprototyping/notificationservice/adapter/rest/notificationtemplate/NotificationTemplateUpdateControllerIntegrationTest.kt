package org.kybprototyping.notificationservice.adapter.rest.notificationtemplate

import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import org.junit.jupiter.api.Test
import org.kybprototyping.notificationservice.domain.common.DataNotFoundFailure
import org.kybprototyping.notificationservice.domain.common.UseCaseHandler
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplateUpdateInput
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(controllers = [NotificationTemplateUpdateController::class])
internal class NotificationTemplateUpdateControllerIntegrationTest {
    private val objetMapper = ObjectMapper() // TODO: Use the common one!

    @MockkBean
    private lateinit var useCaseHandler: UseCaseHandler<NotificationTemplateUpdateInput, Unit>

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `should update an existing notification template by given ID`() {
        // given
        val id = 1
        coEvery {
            useCaseHandler.handle(
                NotificationTemplateUpdateInput(
                    id = id,
                    subject = testBody.subject,
                    content = testBody.content,
                ),
            )
        } returns Unit.right()

        // when & then
        webTestClient.patch()
            .uri { it.path("/api/v1/notification-template/$id").build() }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objetMapper.writeValueAsString(testBody))
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .isEmpty
    }

    @Test
    fun `should return not found when the notification template to update couldn't be found by given ID`() {
        // given
        val id = 1
        coEvery {
            useCaseHandler.handle(
                NotificationTemplateUpdateInput(
                    id = id,
                    subject = testBody.subject,
                    content = testBody.content,
                ),
            )
        } returns DataNotFoundFailure("No notification template found to update by given ID: 1").left()

        // when & then
        webTestClient.patch()
            .uri { it.path("/api/v1/notification-template/$id").build() }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objetMapper.writeValueAsString(testBody))
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
                    "detail": "No notification template found to update by given ID: 1",
                    "instance": "/api/v1/notification-template/$id"
                }
                """.trimIndent(),
            )
    }

    @Test
    fun `should return internal server error when unexpected exception is thrown during notification template update use case execution`() {
        // given
        val id = 1
        coEvery {
            useCaseHandler.handle(
                NotificationTemplateUpdateInput(
                    id = id,
                    subject = testBody.subject,
                    content = testBody.content,
                ),
            )
        } throws RuntimeException("Unexpected exception occurred!")

        // when & then
        webTestClient.patch()
            .uri { it.path("/api/v1/notification-template/$id").build() }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objetMapper.writeValueAsString(testBody))
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
        private val testBody =
            NotificationTemplateUpdateRequest(
                subject = "Updated Subject",
                content = "Updated Content",
            )
    }
}
