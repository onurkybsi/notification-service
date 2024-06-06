package org.kybprototyping.notificationservice.adapter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = [
	"org.kybprototyping.notificationservice.adapter",
	"org.kybprototyping.notificationservice.domain"
])
internal class PrimaryApplication

fun main(args: Array<String>) {
	runApplication<PrimaryApplication>(*args)
}
