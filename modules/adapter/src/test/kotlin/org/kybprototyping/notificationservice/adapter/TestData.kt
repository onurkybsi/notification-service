package org.kybprototyping.notificationservice.adapter

import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jooq.JSON
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.tables.records.ServiceTaskRecord
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.enums.ServiceTaskStatus as RecordServiceTaskStatus
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.enums.ServiceTaskType as RecordServiceTaskType
import org.kybprototyping.notificationservice.domain.model.*
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.UUID

internal object TestData {
    private val objectMapper = jacksonObjectMapper()

    internal val notificationTemplate =
        NotificationTemplate(
            id = 1,
            channel = NotificationChannel.EMAIL,
            type = NotificationType.WELCOME,
            language = NotificationLanguage.EN,
            subject = "Welcome",
            content = "Welcome to our platform \${firstName}",
            modifiedBy = null,
            modifiedAt = OffsetDateTime.parse("2024-10-01T09:00:00Z"),
            createdBy = null,
            createdAt = OffsetDateTime.parse("2024-10-01T09:00:00Z"),
        )

    internal val serviceTask =
        ServiceTask(
            id = UUID.randomUUID(),
            type = ServiceTaskType.SEND_EMAIL,
            status = ServiceTaskStatus.PENDING,
            externalId = UUID.randomUUID(),
            priority = ServiceTaskPriority.MEDIUM,
            executionCount = 0,
            executionStartedAt = null,
            executionScheduledAt = null,
            context = objectMapper
                .createObjectNode()
                .set("input", objectMapper.createObjectNode().set("inputField", TextNode("inputValue"))),
            message = null,
            modifiedAt = OffsetDateTime.parse("2024-10-01T09:00:00Z"),
            createdAt = OffsetDateTime.parse("2024-10-01T09:00:00Z"),
        )

    internal fun serviceTaskRecord(
        id: UUID = UUID.randomUUID(),
        type: RecordServiceTaskType = RecordServiceTaskType.SEND_EMAIL,
        status: RecordServiceTaskStatus = RecordServiceTaskStatus.PENDING,
        externalId: UUID = UUID.randomUUID(),
        priority: Short = ServiceTaskPriority.MEDIUM.value().toShort(),
        executionCount: Short = 0,
        executionStartedAt: LocalDateTime? = null,
        executionScheduledAt: LocalDateTime? = null,
        context: JSON? = JSON.valueOf("{\"input\":{\"inputField\":\"inputValue\"},\"output\":null}"),
        message: String? = null,
        modifiedAt: LocalDateTime = OffsetDateTime.parse("2024-10-01T09:00:00Z").toLocalDateTime(),
        createdAt: LocalDateTime = OffsetDateTime.parse("2024-10-01T09:00:00Z").toLocalDateTime(),
    ) =
        ServiceTaskRecord().also {
            it.id = id
            it.type = type
            it.status = status
            it.externalId = externalId
            it.priority = priority
            it.executionCount = executionCount
            it.executionStartedAt = executionStartedAt
            it.executionScheduledAt = executionScheduledAt
            it.context = context
            it.message = message
            it.modifiedAt = modifiedAt
            it.createdAt = createdAt
        }
}
