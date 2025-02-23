package org.kybprototyping.notificationservice.adapter.monitoring

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.semconv.ServiceAttributes
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
internal class OtelSpringConfiguration {
    @Bean
    internal fun otelCustomizer(
        @Value("\${spring.application.name}") serviceName: String,
    ): AutoConfigurationCustomizerProvider =
        AutoConfigurationCustomizerProvider { customizer ->
            val resource =
                Resource.builder()
                    .put(ServiceAttributes.SERVICE_NAME, serviceName)
                    .build()

            customizer.addLoggerProviderCustomizer { b, _ ->
                b.setResource(resource)
                b.addLogRecordProcessor { _, logRecord ->
                    logRecord.setAttribute(AttributeKey.stringKey("thread"), Thread.currentThread().name)
                }
            }
            customizer.addMeterProviderCustomizer { b, _ ->
                b.setResource(resource)
            }
            customizer.addTracerProviderCustomizer { b, _ ->
                b.setResource(resource)
            }
        }
}
