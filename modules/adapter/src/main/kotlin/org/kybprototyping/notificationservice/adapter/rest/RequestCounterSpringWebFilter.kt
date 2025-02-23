package org.kybprototyping.notificationservice.adapter.rest

import org.kybprototyping.notificationservice.adapter.monitoring.RestMonitor
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
internal class RequestCounterSpringWebFilter(private val restMonitor: RestMonitor) : WebFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        // TODO: "1" shouldn't be included to the metric in case "/api/v1/notification-template/1"!
        restMonitor.increaseRequestCounter(exchange.request.path.value())
        return chain.filter(exchange)
    }
}
