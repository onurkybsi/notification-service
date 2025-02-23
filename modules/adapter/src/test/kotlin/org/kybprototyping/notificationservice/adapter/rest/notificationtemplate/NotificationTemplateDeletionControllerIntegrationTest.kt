package org.kybprototyping.notificationservice.adapter.rest.notificationtemplate

import arrow.core.left
import arrow.core.right
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.kybprototyping.notificationservice.domain.common.DataNotFoundFailure
import org.kybprototyping.notificationservice.domain.common.UseCaseHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(controllers = [NotificationTemplateDeletionController::class])
@Disabled
internal class NotificationTemplateDeletionControllerIntegrationTest {
    @MockkBean
    private lateinit var useCaseHandler: UseCaseHandler<Int, Unit>

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `should delete a notification template`() {
        // given
        val id = 1
        coEvery { useCaseHandler.handle(id) } returns Unit.right()

        // when & then
        webTestClient.delete()
            .uri { it.path("/api/v1/notification-template/$id").build() }
            .exchange()
            .expectStatus()
            .isNoContent
            .expectBody()
            .isEmpty
    }

    @Test
    fun `should return not found when the notification template to delete couldn't be found by given ID`() {
        // given
        val id = 1
        coEvery { useCaseHandler.handle(id) }
            .returns(DataNotFoundFailure("No notification template found to delete by given ID: 1").left())

        // when & then
        webTestClient.delete()
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
                    "detail": "No notification template found to delete by given ID: 1",
                    "instance": "/api/v1/notification-template/$id"
                }
                """.trimIndent(),
            )
    }

    @Test
    fun `should return internal server error when unexpected exception is thrown during notification template deletion use case execution`() {
        // given
        val id = 1
        coEvery { useCaseHandler.handle(id) } throws RuntimeException("Unexpected exception occurred!")

        // when & then
        webTestClient.delete()
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
}
