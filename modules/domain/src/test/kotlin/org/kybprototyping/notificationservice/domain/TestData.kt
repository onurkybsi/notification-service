package org.kybprototyping.notificationservice.domain

import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.kybprototyping.notificationservice.domain.model.*
import java.time.OffsetDateTime
import java.util.*

internal object TestData {
    private val objectMapper = ObjectMapper()

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

    internal fun serviceTask(
        id: UUID = UUID.randomUUID(),
        type: ServiceTaskType = ServiceTaskType.SEND_EMAIL,
        status: ServiceTaskStatus = ServiceTaskStatus.PENDING,
        externalId: UUID = UUID.randomUUID(),
        priority: ServiceTaskPriority = ServiceTaskPriority.MEDIUM,
        executionCount: Int = 0,
        executionStartedAt: OffsetDateTime? = null,
        executionScheduledAt: OffsetDateTime? = null,
        input: TreeNode? = objectMapper.createObjectNode().put("inputField", "inputValue"),
        output: TreeNode? = null,
        message: String? = null,
        modifiedAt: OffsetDateTime = OffsetDateTime.parse("2024-10-01T09:00:00Z"),
        createdAt: OffsetDateTime = OffsetDateTime.parse("2024-10-01T09:00:00Z"),
    ) =
        ServiceTask(
            id = id,
            type = type,
            status = status,
            externalId = externalId,
            priority = priority,
            executionCount = executionCount,
            executionStartedAt = executionStartedAt,
            executionScheduledAt = executionScheduledAt,
            input = input,
            output = output,
            message = message,
            modifiedAt = modifiedAt,
            createdAt = createdAt,
        )
}
