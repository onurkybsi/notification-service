package org.kybprototyping.notificationservice.adapter

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
	scanBasePackages = [
		"org.kybprototyping.notificationservice.adapter",
		"org.kybprototyping.notificationservice.domain"
	]
)
@OpenAPIDefinition(
	info = Info(
		title = "Notification Service",
		version = "0.1.0",
	)
)
class NotificationServiceApplication

fun main(args: Array<String>) {
	runApplication<NotificationServiceApplication>(*args)
}
