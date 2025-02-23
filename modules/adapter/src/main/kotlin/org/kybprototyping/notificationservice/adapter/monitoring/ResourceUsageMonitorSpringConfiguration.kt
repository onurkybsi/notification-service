package org.kybprototyping.notificationservice.adapter.monitoring

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.metrics.ObservableLongGauge
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.lang.management.ManagementFactory

@Configuration
internal class ResourceUsageMonitorSpringConfiguration {
    // io.micrometer:micrometer-registry-otlp already exports resource metrics to OTEL Collector.
    // This is just an example how to do this through io.opentelemetry APIs.
    @Bean
    internal fun customMemoryUsageMonitor(openTelemetry: OpenTelemetry): ObservableLongGauge =
        openTelemetry
            .getMeter("org.kybprototyping.notificationservice.adapter")
            .gaugeBuilder("memory_used")
            .setDescription("Current memory usage in bytes")
            .setUnit("bytes")
            .ofLongs()
            .buildWithCallback { it.record(ManagementFactory.getMemoryMXBean().heapMemoryUsage.used) }
}
