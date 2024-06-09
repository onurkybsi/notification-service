package org.kybprototyping.notificationservice.common

import java.util.HashMap

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
     * @return **true** if there are some failures, otherwise **false**.
     */
    fun  isNotValid(): Boolean {
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

    companion object {
        /**
         * Builds a [ValidationResult] from given [failures].
         *
         * @param failures validation failures as __field name__ and __failure message__ pairs.
         * @return built [ValidationResult]
         */
        @JvmStatic
        fun from(vararg failures: Pair<String, Array<String>>): ValidationResult {
            val validationResult = ValidationResult()
            failures.forEach { failure ->
                failure.second.forEach {
                    validationResult.addFailure(failure.first, it)
                }
            }
            return validationResult
        }
    }
}
