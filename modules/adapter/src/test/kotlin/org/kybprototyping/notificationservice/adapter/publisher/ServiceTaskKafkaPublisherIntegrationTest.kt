package org.kybprototyping.notificationservice.adapter.publisher

import io.kotest.assertions.arrow.core.shouldBeRight
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.kybprototyping.notificationservice.domain.model.ServiceTaskType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import java.time.Duration
import java.util.UUID

@SpringBootTest(
    classes = [
        KafkaAutoConfiguration::class,
        ServiceTaskKafkaPublisher.Properties::class,
        ServiceTaskKafkaPublisher::class,
    ],
    properties = [
        "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.ByteArraySerializer",
        "spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.ByteArrayDeserializer",
        "spring.kafka.consumer.group-id=test",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "ports.service-task-publisher.topicNames.SEND_EMAIL=sent-email"
    ]
)
@EmbeddedKafka(partitions = 1, brokerProperties = ["listeners=PLAINTEXT://localhost:9092", "port=9092" ])
@DirtiesContext
internal class ServiceTaskKafkaPublisherIntegrationTest {
    @Autowired
    private lateinit var consumerFactory: ConsumerFactory<String, ByteArray>

    @Autowired
    private lateinit var underTest: ServiceTaskKafkaPublisher

    @Test
    fun `should publish given task output through Kafka broker`() = runTest {
        // given
        val type = ServiceTaskType.SEND_EMAIL
        val externalId = UUID.randomUUID()
        val output: ByteArray = "It's done!".toByteArray()

        // when
        val actual = underTest.execute(type, externalId, output)

        // then
        actual shouldBeRight Unit
        val consumer = buildSentEmailConsumer()
        val recordsConsumed = consumer.poll(Duration.ofSeconds(5))
        assertThat(recordsConsumed.count()).isEqualTo(1)
        val sentEmailRecords = recordsConsumed.records("sent-email").toList()
        assertThat(sentEmailRecords.size).isEqualTo(1)
        assertThat(sentEmailRecords[0].key()).isEqualTo(externalId.toString())
        assertThat(sentEmailRecords[0].value()).isEqualTo(output)
    }

    private fun buildSentEmailConsumer() =
        consumerFactory
            .createConsumer()
            .also { consumer ->
                consumer.subscribe(listOf("sent-email"))
            }
}
