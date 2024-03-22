package org.kybprototyping.notificatin_service.domain.interfaces

import java.util.*
import kotlin.collections.ArrayList

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

        /**
         * Returns whether there are failures or not.
         *
         * @return **true** if there is no failure, otherwise **false**.
         */
        fun  isValid(): Boolean {
            return failures.isEmpty()
        }

        /**
         * Returns the added failures.
         *
         * @return added failures
         */
        fun getFailures(): HashMap<String, ArrayList<String>> {
            val copy = HashMap<String, ArrayList<String>>()
            failures.forEach { f -> copy[f.key] = ArrayList(f.value) }
            return  copy
        }

    }

}