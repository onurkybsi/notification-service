package org.kybprototyping.notificationservice.domain.model

/**
 * Represents a _Notification Service_ task statuses.
 */
enum class ServiceTaskStatus {
    PENDING,
    IN_PROGRESS,
    ERROR,
    COMPLETED,
    FAILED,
    PUBLISHED,
}
