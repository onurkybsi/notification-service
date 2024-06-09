package org.kybprototyping.notificationservice.domain.model

import java.time.OffsetDateTime
import java.util.UUID

/**
 * Represents the _Notification Service_'s asynchronous tasks.
 */
data class NotificationServiceTask(
    val id: UUID,
    val type: NotificationServiceTaskType,
    val status: NotificationServiceTaskStatus,
    val executionDateTime: OffsetDateTime?,
    val tryCount: Int,
    val input: Any?,
    val output: Any?,
    val message: String?,
    val modificationDate: OffsetDateTime,
    val creationDate: OffsetDateTime
)