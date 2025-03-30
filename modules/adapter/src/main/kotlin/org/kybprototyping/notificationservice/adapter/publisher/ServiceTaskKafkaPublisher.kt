package org.kybprototyping.notificationservice.adapter.publisher

import arrow.core.right
import kotlinx.coroutines.future.await
import org.kybprototying.notificationservice.common.runExceptionCatching
import org.kybprototyping.notificationservice.domain.model.ServiceTaskType
import org.kybprototyping.notificationservice.domain.port.ServiceTaskPublisherPort
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
internal data class ServiceTaskKafkaPublisher(
    private val properties: Properties,
    private val kafkaTemplate: KafkaTemplate<String, ByteArray>,
) : ServiceTaskPublisherPort {
    override suspend fun execute(type: ServiceTaskType, externalId: UUID, output: ByteArray) =
        runExceptionCatching {
            kafkaTemplate
                .send(properties.topicNames[type]!!, externalId.toString(), output)
                .await()
                .let { }
                .right()
        }

    @Component
    @ConfigurationProperties(prefix = "ports.service-task-publisher")
    internal data class Properties(val topicNames: Map<ServiceTaskType, String>)
}
