package org.kybprototying.notificationservice.common

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.util.HashMap

/**
 * Represents the state of a validation process.
 *
 * @author o.kayabasi@outlook.com
 */
@JsonSerialize(using = ValidationResult.Serializer::class)
class ValidationResult {

    private val failures: HashMap<String, ArrayList<String>> = HashMap<String, ArrayList<String>>()

    /**
     * Adds a new failure message for the field with given name.
     *
     * @param name field name
     * @param message failure message
     */
    fun failure(name: String, message: String) {
        val fieldFailures = failures.getOrDefault(name, ArrayList())
        fieldFailures.add(message)
        failures[name] = fieldFailures
    }

    /**
     * Returns whether there are failures or not.
     *
     * @return **true** if there are some failures, otherwise **false**.
     */
    fun isNotValid(): Boolean = failures.isNotEmpty()

    /**
     * Returns the added failures.
     *
     * @return added failures
     */
    fun failures(): HashMap<String, ArrayList<String>> {
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
        fun from(vararg failures: Pair<String, Array<String>>): ValidationResult {
            val validationResult = ValidationResult()
            failures.forEach { failure ->
                failure.second.forEach {
                    validationResult.failure(failure.first, it)
                }
            }
            return validationResult
        }
    }

    internal class Serializer : JsonSerializer<ValidationResult>() {
        override fun serialize(p0: ValidationResult?, p1: JsonGenerator?, p2: SerializerProvider?) {
            p1?.writeStartObject()
            p0?.isNotValid()?.let {
                p1?.writeBooleanField("isValid", !it)
            }
            p0?.failures?.let {
                p1?.writeObjectField("failures", it)
            }
            p1?.writeEndObject()
        }
    }
}
