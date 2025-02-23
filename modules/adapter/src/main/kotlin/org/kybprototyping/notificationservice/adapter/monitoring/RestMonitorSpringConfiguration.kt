package org.kybprototyping.notificationservice.adapter.monitoring

import io.opentelemetry.api.OpenTelemetry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
internal class RestMonitorSpringConfiguration {
    @Bean
    internal fun restMonitor(openTelemetry: OpenTelemetry): RestMonitor {
        val meter = openTelemetry.getMeter("org.kybprototyping.notificationservice.adapter")
        return RestMonitor(meter.counterBuilder("request_counter").build())
    }
}
