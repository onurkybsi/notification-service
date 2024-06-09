package org.kybprototyping.notificationservice.domain.model

/**
 * Represents a _Notification Service_ task statuses.
 */
enum class NotificationServiceTaskStatus {

    CREATED,
    EXECUTING,
    EXECUTED,
    TO_BE_RETRIED,
    CANCELLED,
    FAILED

}