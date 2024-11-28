package org.kybprototying.notificationservice.common

/**
 * Simple generic interface for validators.
 */
interface Validator<T> {
    /**
     * Validates the given object under its validation rules.
     *
     * @param validated object to be validated
     * @return validation result
     */
    fun validate(validated: T): ValidationResult
}
