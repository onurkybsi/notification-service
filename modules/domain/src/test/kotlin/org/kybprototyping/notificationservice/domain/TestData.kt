package org.kybprototyping.notificationservice.domain

import org.kybprototyping.notificationservice.domain.model.NotificationChannel
import org.kybprototyping.notificationservice.domain.model.NotificationLanguage
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate
import org.kybprototyping.notificationservice.domain.model.NotificationType
import java.time.OffsetDateTime

internal object TestData {
    internal val notificationTemplate = NotificationTemplate(
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
}