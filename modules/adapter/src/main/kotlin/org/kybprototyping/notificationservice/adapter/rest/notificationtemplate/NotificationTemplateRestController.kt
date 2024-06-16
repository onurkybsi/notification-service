package org.kybprototyping.notificationservice.adapter.rest.notificationtemplate

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.kybprototyping.notificationservice.adapter.rest.InvalidRequestResponse
import org.kybprototyping.notificationservice.adapter.rest.NonExistentResourceResponse
import org.kybprototyping.notificationservice.adapter.rest.OkApiResponse
import org.kybprototyping.notificationservice.domain.model.*
import org.kybprototyping.notificationservice.domain.common.interfaces.InputOnlyUseCaseHandler
import org.kybprototyping.notificationservice.domain.common.interfaces.InputOutputUseCaseHandler
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.creation.NotificationTemplateCreationInput
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.creation.NotificationTemplateCreationOutput
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.retrieval.NotificationTemplatesRetrievalInput
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.update.NotificationTemplateUpdateInput
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/v1/notification-template")
@Tag(
    name = "notification-template",
    description = "Notification Template API v1"
)
internal class NotificationTemplateRestController(
    private val notificationTemplateCreationUseCaseHandler:
        InputOutputUseCaseHandler<NotificationTemplateCreationInput, NotificationTemplateCreationOutput>,
    private val notificationTemplatesRetrievalUseCaseHandler:
        InputOutputUseCaseHandler<NotificationTemplatesRetrievalInput, List<NotificationTemplate>>,
    private val notificationTemplateRetrievalUseCaseHandler:
        InputOutputUseCaseHandler<Int, NotificationTemplate>,
    private val notificationTemplateUpdateUseCaseHandler:
        InputOutputUseCaseHandler<NotificationTemplateUpdateInput, NotificationTemplate>,
    private val notificationTemplateDeletionUseCaseHandler:
        InputOnlyUseCaseHandler<Int>
) {

    @PostMapping
    @Operation(summary = "Creates a notification template.")
    @ApiResponse(
        responseCode = "201",
        description = "Successful creation",
        headers = [
            Header(name = "location", description = "Location to get the created notification template")
        ]
    )
    @InvalidRequestResponse
    internal suspend fun createNotificationTemplate(@RequestBody body: NotificationTemplateCreationInput): ResponseEntity<Nothing>
        = notificationTemplateCreationUseCaseHandler.handle(body)
            .let { ResponseEntity.created(URI.create("/api/v1/notification-template/${it.id}")).build() }

    @GetMapping
    @Operation(summary = "Returns the notification templates with given values.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Successful retrieval",
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = ArraySchema(schema = Schema(implementation = NotificationTemplate::class))
                )
            ]
        )
    )
    internal suspend fun getNotificationTemplates(
        @RequestParam(required = false) channel: NotificationChannel?,
        @RequestParam(required = false) type: NotificationType?,
        @RequestParam(required = false) language: NotificationLanguage?
    ): ResponseEntity<List<NotificationTemplate>> =
        ResponseEntity.ok(
            notificationTemplatesRetrievalUseCaseHandler.handle(
                NotificationTemplatesRetrievalInput(
                    channel = channel,
                    type = type,
                    language = language
                )
        ))

    @GetMapping("/{id}")
    @Operation(summary = "Returns the notification template with given ID.")
    @OkApiResponse
    @NonExistentResourceResponse
    internal suspend fun getNotificationTemplate(@PathVariable id: Int): ResponseEntity<NotificationTemplate> =
        ResponseEntity.ok(notificationTemplateRetrievalUseCaseHandler.handle(id))

    @PatchMapping("/{id}")
    @Operation(summary = "Updates content of the notification template with given ID.")
    @ApiResponse(
        responseCode = "200",
        description = "Successful update",
        content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)]
    )
    @NonExistentResourceResponse
    internal suspend fun updateNotificationTemplate(@PathVariable id: Int, @RequestBody body: NotificationTemplateUpdateRequest) =
        ResponseEntity.ok(
            notificationTemplateUpdateUseCaseHandler.handle(NotificationTemplateUpdateInput(
                id = id,
                subject = body.subject,
                content = body.content
            ))
        )

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes the notification template with given ID.")
    @ApiResponse(responseCode = "204", description = "Successful deletion")
    @NonExistentResourceResponse
    internal suspend fun deleteNotificationTemplate(@PathVariable id: Int): ResponseEntity<Nothing> {
        notificationTemplateDeletionUseCaseHandler.handle(id)
        return ResponseEntity.noContent().build()
    }

}