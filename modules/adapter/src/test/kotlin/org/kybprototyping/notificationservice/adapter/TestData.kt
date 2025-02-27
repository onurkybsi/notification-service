package org.kybprototyping.notificationservice.adapter

import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jayway.jsonpath.internal.filter.ValueNodes.StringNode
import org.kybprototyping.notificationservice.domain.model.*
import java.time.OffsetDateTime
import java.util.UUID

internal object TestData {
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
            input = jacksonObjectMapper().createObjectNode().set("inputField", TextNode("inputValue")),
            output = null,
            message = null,
            modifiedAt = OffsetDateTime.parse("2024-10-01T09:00:00Z"),
            createdAt = OffsetDateTime.parse("2024-10-01T09:00:00Z"),
        )
}
