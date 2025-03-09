package org.kybprototyping.notificationservice.adapter

import org.kybprototying.notificationservice.common.TimeUtils
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@TestConfiguration
internal class TimeUtilsSpringConfiguration {
    @Bean
    internal fun timeUtils() = TimeUtils(Clock.fixed(Instant.parse("2025-01-01T12:00:00Z"), ZoneId.of("UTC")))
}
