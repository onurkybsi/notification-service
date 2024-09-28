package org.kybprototyping.notificationservice.adapter

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.kotlin.Logging
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.boot.runApplication


@SpringBootApplication(
	scanBasePackages = [
		"org.kybprototyping.notificationservice.adapter",
		"org.kybprototyping.notificationservice.domain"
	],
	exclude = [R2dbcAutoConfiguration::class] // TODO: We do it manually, should we?
)
@OpenAPIDefinition(
	info = Info(
		title = "Notification Service",
		version = "0.1.0",
	)
)
class NotificationServiceApplication {
	///@Bean
	internal fun sandbox(): CommandLineRunner =
		object : CommandLineRunner, Logging {
			override fun run(vararg args: String?) {
				runBlocking {

				}
			}
		}
}

fun main(args: Array<String>) {
	runApplication<NotificationServiceApplication>(*args)
}
