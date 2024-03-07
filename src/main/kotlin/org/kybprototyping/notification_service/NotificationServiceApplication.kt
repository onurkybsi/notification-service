package org.kybprototyping.notification_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NotificationServiceApplication

fun main(args: Array<String>) {
	// '*' (spread operator) copies the args array, it's used to convert arrays to varargs parameters.
	runApplication<NotificationServiceApplication>(*args)
}
