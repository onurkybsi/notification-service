package org.kybprototyping.notificationservice.adapter.rest.notification

import arrow.core.left
import arrow.core.right
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kybprototying.notificationservice.common.DataConflictFailure
import org.kybprototying.notificationservice.common.DataInvalidityFailure
import org.kybprototying.notificationservice.common.UnexpectedFailure.Companion.unexpectedFailure
import org.kybprototying.notificationservice.common.ValidationResult
import org.kybprototyping.notificationservice.adapter.monitoring.RestMonitor
import org.kybprototyping.notificationservice.domain.common.UseCaseHandler
import org.kybprototyping.notificationservice.domain.model.NotificationLanguage
import org.kybprototyping.notificationservice.domain.model.NotificationType
import org.kybprototyping.notificationservice.domain.usecase.notification.SendEmailInput
import org.kybprototyping.notificationservice.domain.usecase.notification.SendEmailOutput
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.*

@WebFluxTest(controllers = [NotificationRestController::class])
internal class NotificationRestControllerIntegrationTest {
    private var requestCounter = 0

    @MockkBean
    private lateinit var restMonitor: RestMonitor

    @MockkBean
    private lateinit var useCaseHandler: UseCaseHandler<SendEmailInput, SendEmailOutput>

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setUp() {
        requestCounter = 0
        every { restMonitor.increaseRequestCounter("/api/v1/notification/email", "POST") } answers { requestCounter++ }
    }

    @Test
    fun `should submit a send email task`() {
        // given
        val expectedInput =
            SendEmailInput(
                type = NotificationType.WELCOME,
                language = NotificationLanguage.EN,
                to = "recipient@gmail.com",
                values = mapOf("firstName" to "Onur"),
                externalId = UUID.randomUUID()
            )
        coEvery { useCaseHandler.handle(any()) } returns SendEmailOutput(expectedInput.externalId!!).right()

        // when & then
        webTestClient.post()
            .uri { it.path("/api/v1/notification/email").build() }
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Idempotency-Key", expectedInput.externalId.toString())
            .bodyValue(
                """
                    {
                        "type": "WELCOME",
                        "language": "EN",
                        "to": "recipient@gmail.com",
                        "values": {
                            "firstName": "Onur"
                        }
                    }
                """.trimIndent()
            )
            .exchange()
            .expectStatus()
            .isAccepted
            .expectBody()
            .json(
                """
                    {
                        "id": "${expectedInput.externalId}"
                    }
                """.trimIndent()
            )
        assertThat(requestCounter).isEqualTo(1)
    }

    @Test
    fun `should return bad request when given request is not valid`() {
        // given
        val validationResult = ValidationResult.from("to" to arrayOf("must be an email"))
        coEvery { useCaseHandler.handle(any()) }
            .returns(DataInvalidityFailure("Request is not valid!", validationResult).left())

        // when & then
        webTestClient.post()
            .uri { it.path("/api/v1/notification/email").build() }
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Idempotency-Key", UUID.randomUUID().toString())
            .bodyValue(
                """
                    {
                        "type": "WELCOME",
                        "language": "EN",
                        "to": "recipient",
                        "values": {
                            "firstName": "Onur"
                        }
                    }
                """.trimIndent()
            )
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .json(
                """
                    {
                        "type": "about:blank",
                        "title": "Bad Request",
                        "status": 400,
                        "detail": "Bad Request",
                        "instance": "/api/v1/notification/email",
                        "validationResult": {
                          "isValid": false,
                          "failures": {
                            "to": [
                              "must be an email"
                            ]
                          }
                        }
                    }
                """.trimIndent()
            )
        assertThat(requestCounter).isEqualTo(1)
    }

    @Test
    fun `should return conflict when given idempotency key is already processed`() {
        // given
        coEvery { useCaseHandler.handle(any()) }
            .returns(DataConflictFailure("Same request already processed!").left())

        // when & then
        webTestClient.post()
            .uri { it.path("/api/v1/notification/email").build() }
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Idempotency-Key", UUID.randomUUID().toString())
            .bodyValue(
                """
                    {
                        "type": "WELCOME",
                        "language": "EN",
                        "to": "recipient",
                        "values": {
                            "firstName": "Onur"
                        }
                    }
                """.trimIndent()
            )
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.CONFLICT)
            .expectBody()
            .json(
                """
                    {
                        "type": "about:blank",
                        "title": "Conflict",
                        "status": 409,
                        "detail": "Same request already processed!",
                        "instance": "/api/v1/notification/email"
                    }
                """.trimIndent()
            )
        assertThat(requestCounter).isEqualTo(1)
    }

    @Test
    fun `should return internal server error when unexpected failure occurred`() {
        // given
        coEvery { useCaseHandler.handle(any()) } returns unexpectedFailure.left()

        // when & then
        webTestClient.post()
            .uri { it.path("/api/v1/notification/email").build() }
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Idempotency-Key", UUID.randomUUID().toString())
            .bodyValue(
                """
                    {
                        "type": "WELCOME",
                        "language": "EN",
                        "to": "recipient",
                        "values": {
                            "firstName": "Onur"
                        }
                    }
                """.trimIndent()
            )
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
                        "detail": "Something went unexpectedly wrong!",
                        "instance": "/api/v1/notification/email"
                    }
                """.trimIndent()
            )
        assertThat(requestCounter).isEqualTo(1)
    }
}
