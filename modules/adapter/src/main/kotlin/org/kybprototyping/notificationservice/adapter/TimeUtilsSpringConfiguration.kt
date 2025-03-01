package org.kybprototyping.notificationservice.adapter

import org.kybprototying.notificationservice.common.TimeUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
internal class TimeUtilsSpringConfiguration {
    @Bean
    internal fun timeUtils() = TimeUtils(Clock.systemUTC())
}