package org.kybprototyping.notificationservice.domain.model

import com.fasterxml.jackson.core.TreeNode
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Represents the _Notification Service_'s asynchronous tasks.
 */
data class ServiceTask(
    val id: UUID,
    val type: ServiceTaskType,
    val status: ServiceTaskStatus,
    val externalId: UUID,
    val priority: ServiceTaskPriority,
    val executionCount: Int, // Increased after execution!
    val executionStartedAt: OffsetDateTime?,
    val executionScheduledAt: OffsetDateTime?,
    val context: TreeNode?,
    val message: String?,
    val modifiedAt: OffsetDateTime,
    val createdAt: OffsetDateTime,
)
