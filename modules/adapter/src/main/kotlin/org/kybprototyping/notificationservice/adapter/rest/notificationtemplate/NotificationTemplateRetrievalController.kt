package org.kybprototyping.notificationservice.adapter.rest.notificationtemplate

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import org.kybprototyping.notificationservice.adapter.rest.common.InternalServerErrorApiResponse
import org.kybprototyping.notificationservice.adapter.rest.common.NotFoundApiResponse
import org.kybprototyping.notificationservice.adapter.rest.common.OkApiResponse
import org.kybprototyping.notificationservice.adapter.rest.common.ResponseEntityUtils.toResponseEntity
import org.kybprototyping.notificationservice.domain.common.UseCaseHandler
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate as DomainNotificationTemplate
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/notification-template")
@Tag(
    name = "notification-template",
    description = "Notification Template API v1"
)
internal class NotificationTemplateRetrievalController(
    private val notificationTemplateRetrievalUseCase: UseCaseHandler<Int, DomainNotificationTemplate>
) {
    @GetMapping
    @Operation(summary = "Returns the notification templates with given values.")
    @OkApiResponse(
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = ArraySchema(schema = Schema(implementation = NotificationTemplate::class))
            )
        ]
    )
    @InternalServerErrorApiResponse
    internal suspend fun getNotificationTemplates(
        @RequestParam(required = false) channel: NotificationChannel?,
        @RequestParam(required = false) type: NotificationType?,
        @RequestParam(required = false) language: NotificationLanguage?
    ): ResponseEntity<*> = TODO("Will be implemented...")

    @GetMapping("/{id}")
    @Operation(summary = "Returns the notification template with given ID.")
    @OkApiResponse(content = [
        Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = NotificationTemplate::class)
        )
    ])
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    internal suspend fun getNotificationTemplate(@PathVariable id: Int): ResponseEntity<*> =
        notificationTemplateRetrievalUseCase.handle(id)
            .fold(
                ifLeft = { it.toResponseEntity() },
                ifRight = { ResponseEntity.ok(it) }
            )
}