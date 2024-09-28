package org.kybprototyping.notificationservice.domain.common

import org.kybprototying.notificationservice.common.ValidationResult

/**
 * Base type for the *Notification Service* failures.
 */
sealed class Failure(
    open val message: String,
    open val isTemporary: Boolean,
    open val cause: Throwable? = null
)

/**
 * Failure that indicates that the given input has invalid values.
 */
data class DataInvalidityFailure(
    override val message: String,
    val validationResult: ValidationResult
) : Failure(message, false)