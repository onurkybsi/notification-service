package org.kybprototyping.notificationservice.adapter.monitoring

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.metrics.LongCounter

/**
 * Represents the API that provides monitoring for REST APIs.
 */
internal class RestMonitor(private val requestCounter: LongCounter) {
    internal fun increaseRequestCounter(path: String, method: String): Unit =
        requestCounter.add(
            1,
            Attributes.of(
                AttributeKey.stringKey("path"), path,
                AttributeKey.stringKey("method"), method,
            ),
        )
}
