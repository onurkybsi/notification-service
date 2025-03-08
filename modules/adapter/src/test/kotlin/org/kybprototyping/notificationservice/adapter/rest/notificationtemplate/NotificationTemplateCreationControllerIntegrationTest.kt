package org.kybprototyping.notificationservice.adapter.rest.notificationtemplate

import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.every
import org.apache.commons.lang3.StringUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kybprototying.notificationservice.common.DataConflictFailure
import org.kybprototyping.notificationservice.adapter.monitoring.RestMonitor
import org.kybprototyping.notificationservice.adapter.rest.notificationtemplate.NotificationChannel.Companion.toDomain
import org.kybprototyping.notificationservice.adapter.rest.notificationtemplate.NotificationLanguage.Companion.toDomain
import org.kybprototyping.notificationservice.adapter.rest.notificationtemplate.NotificationType.Companion.toDomain
import org.kybprototyping.notificationservice.domain.common.UseCaseHandler
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplateCreationInput
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(controllers = [NotificationTemplateCreationController::class])
internal class NotificationTemplateCreationControllerIntegrationTest {
    private val objetMapper = ObjectMapper() // TODO: Use the common one!
    private var requestCounter = 0

    @MockkBean
    private lateinit var restMonitor: RestMonitor

    @MockkBean
    private lateinit var useCaseHandler: UseCaseHandler<NotificationTemplateCreationInput, Int>

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setUp() {
        requestCounter = 0
        every { restMonitor.increaseRequestCounter("/api/v1/notification-template", "POST") } answers { requestCounter++ }
    }

    @Test
    fun `should create a notification template and return created template ID`() {
        // given
        val expectedInput =
            NotificationTemplateCreationInput(
                channel = testRequestBody.channel.toDomain(),
                type = testRequestBody.type.toDomain(),
                language = testRequestBody.language.toDomain(),
                subject = testRequestBody.subject,
                content = testRequestBody.content,
            )
        coEvery { useCaseHandler.handle(expectedInput) } returns 1.right()

        // when & then
        webTestClient.post()
            .uri { it.path("/api/v1/notification-template").build() }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objetMapper.writeValueAsString(testRequestBody))
            .exchange()
            .expectStatus()
            .isCreated
            .expectHeader()
            .location("/api/v1/notification-template/1")
            .expectBody()
            .isEmpty
        assertThat(requestCounter).isEqualTo(1)
    }

    @Test
    fun `should return conflict when notification template creation fails with DataConflictFailure`() {
        // given
        val expectedInput =
            NotificationTemplateCreationInput(
                channel = testRequestBody.channel.toDomain(),
                type = testRequestBody.type.toDomain(),
                language = testRequestBody.language.toDomain(),
                subject = testRequestBody.subject,
                content = testRequestBody.content,
            )
        coEvery { useCaseHandler.handle(expectedInput) } returns DataConflictFailure("Template is already created!").left()

        // when & then
        webTestClient.post()
            .uri { it.path("/api/v1/notification-template").build() }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objetMapper.writeValueAsString(testRequestBody))
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
                    "detail": "Template is already created!",
                    "instance": "/api/v1/notification-template"
                }
                """.trimIndent(),
            )
        assertThat(requestCounter).isEqualTo(1)
    }

    @Test
    fun `should return internal server error when unexpected exception is thrown during notification template creation use case execution`() {
        // given
        val expectedInput =
            NotificationTemplateCreationInput(
                channel = testRequestBody.channel.toDomain(),
                type = testRequestBody.type.toDomain(),
                language = testRequestBody.language.toDomain(),
                subject = testRequestBody.subject,
                content = testRequestBody.content,
            )
        coEvery { useCaseHandler.handle(expectedInput) } throws RuntimeException("Unexpected exception occurred!")

        // when & then
        webTestClient.post()
            .uri { it.path("/api/v1/notification-template").build() }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objetMapper.writeValueAsString(testRequestBody))
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
        assertThat(requestCounter).isEqualTo(1)
    }

    private companion object {
        val testRequestBody =
            NotificationTemplateCreationRequest(
                channel = NotificationChannel.EMAIL,
                type = NotificationType.WELCOME,
                language = NotificationLanguage.EN,
                subject = StringUtils.EMPTY,
                content = StringUtils.EMPTY,
            )
    }
}
