package org.kybprototyping.notificationservice.infrastructure.primary

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PrimaryApplication

fun main(args: Array<String>) {
	runApplication<PrimaryApplication>(*args)
}
