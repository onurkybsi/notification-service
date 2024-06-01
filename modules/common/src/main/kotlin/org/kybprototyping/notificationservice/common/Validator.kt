package org.kybprototyping.notificationservice.common

/**
 * Simple generic interface for validators.
 */
interface Validator<T> {

    /**
     * Validates given object under the related validation rules.
     *
     * @param validated object to be validated
     * @return validation result
     */
    fun validate(validated: T): ValidationResult

}