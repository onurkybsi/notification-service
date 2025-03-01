package org.kybprototying.notificationservice.common

import java.time.Clock
import java.time.LocalDateTime
import java.time.OffsetDateTime

/**
 * Utils for time-related functions.
 */
data class TimeUtils(private val clock: Clock = Clock.systemUTC()) {
    /**
     * Obtains the current date-time as [LocalDateTime] from the configured [clock].
     *
     * @return current date-time
     */
    fun nowAsLocalDateTime(): LocalDateTime = LocalDateTime.now(clock)

    /**
     * Converts the given [LocalDateTime] to [OffsetDateTime] by configured [clock].
     *
     * @return converted [OffsetDateTime] value
     */
    fun toOffsetDateTime(from: LocalDateTime): OffsetDateTime = from.atZone(clock.zone).toOffsetDateTime()
}
