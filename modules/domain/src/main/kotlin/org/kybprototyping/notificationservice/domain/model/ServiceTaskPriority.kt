package org.kybprototyping.notificationservice.domain.model

import java.lang.IllegalArgumentException

/**
 * Represents a _Notification Service_ task priorities.
 */
enum class ServiceTaskPriority(private val value: Int) {
    HIGH(3),
    MEDIUM(2),
    LOW(1),
    ;

    /**
     * Value of the enum.
     */
    fun value() = this.value

    companion object {
        /**
         * Returns [ServiceTaskPriority] by given [value].
         *
         * @param value integer value of the enum
         * @return [ServiceTaskPriority] value of given [value],
         * *null* if no [ServiceTaskPriority] value exists by given [value]
         */
        fun valueOf(value: Int) =
            when(value) {
                1 -> LOW
                2 -> MEDIUM
                3 -> HIGH
                else -> null
            }
    }
}