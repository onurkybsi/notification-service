package org.kybprototyping.notificatin_service.domain.interfaces

import java.util.*

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

    /**
     * Represents the state of a validation process.
     *
     * @author o.kayabasi@outlook.com
     */
    class ValidationResult {

        private val failures: HashMap<String, ArrayList<String>> = HashMap<String, ArrayList<String>>()

        /**
         * Adds a new failure message for the field with given name.
         *
         * @param name field name
         * @param message failure message
         */
        fun addFailure(name: String, message: String) {
            val fieldFailures = failures.getOrDefault(name, ArrayList())
            fieldFailures.add(message)
            failures[name] = fieldFailures
        }

        fun  isValid(): Boolean {
            return failures.isEmpty()
        }

        override fun toString(): String {
            val failuresStr = StringBuilder("{")
            for (fieldName in failures.keys) {
                failuresStr.append((fieldName + "=" + failures[fieldName]) + ", ")
            }
            failuresStr.delete(failuresStr.length - 2, failuresStr.length).append("}")
            return failuresStr.toString()
        }

    }

}