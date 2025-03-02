package org.kybprototyping.notificationservice.adapter.emailsender

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

@SpringBootTest(
    classes = [SpringConfiguration::class],
    properties = [
        "ports.email-sender.from=test@gmail.com",
        "ports.email-sender.smtp.auth=false",
        "ports.email-sender.smtp.starttls.enable=false",
        "ports.email-sender.smtp.starttls.required=false",
    ]
)
internal class JakartaSenderIntegrationTest {
    @Autowired
    private lateinit var underTest: JakartaSender

    @Test
    fun `should send an email by given values`() = runTest {
        // given
        val from = "test@gmail.com"
        val to = "recipient@gmail.com"
        val subject = "Welcome"
        val content = "Welcome!!"

        // when
        underTest.send(from, to, subject, content)

        // then
        val emailsSent = mailHogClient.get()
            .uri("/messages")
            .retrieve()
            .awaitBody<String>()
            .let { objectMapper.readValue<ObjectNode>(it) }
            .let { it.get("items") as ArrayNode }
        assertThat(emailsSent.size()).isEqualTo(1)

        val emailHeadersSent = emailsSent.get(0).get("Content").get("Headers")
        val emailBodySent = emailsSent.get(0).get("Content").get("Body")
        assertThat(emailHeadersSent.get("From").size()).isEqualTo(1)
        assertThat(emailHeadersSent.get("From").get(0).asText()).isEqualTo("test@gmail.com")
        assertThat(emailHeadersSent.get("To").size()).isEqualTo(1)
        assertThat(emailHeadersSent.get("To").get(0).asText()).isEqualTo("recipient@gmail.com")
        assertThat(emailHeadersSent.get("Subject").size()).isEqualTo(1)
        assertThat(emailHeadersSent.get("Subject").get(0).asText()).isEqualTo("Welcome")
        assertThat(emailBodySent).matches { it.asText().contains("Welcome!!") }
        assertThat(emailBodySent).matches { it.asText().contains("Content-Type: text/html; charset=utf-8") }
    }

    private companion object {
        private val instance = GenericContainer(DockerImageName.parse("mailhog/mailhog:v1.0.1")).withExposedPorts(1025, 8025)
        private val objectMapper = jacksonObjectMapper()

        private lateinit var mailHogClient: WebClient

        @BeforeAll
        @JvmStatic
        fun startMailhogContainer() {
            instance.start()
            mailHogClient = WebClient.create("http://${instance.host}:${instance.getMappedPort(8025)}/api/v2")
        }

        @AfterAll
        @JvmStatic
        fun stopMailhogContainer() {
            instance.stop()
        }

        @DynamicPropertySource
        @JvmStatic
        private fun setSmtpProperties(registry: DynamicPropertyRegistry) {
            registry.add("ports.email-sender.smtp.host") { instance.host }
            registry.add("ports.email-sender.smtp.port") { instance.getMappedPort(1025) }
        }
    }
}
