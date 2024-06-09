package org.kybprototyping.notificationservice.adapter.rest.notificationtemplate

import org.kybprototyping.notificationservice.domain.model.*
import org.kybprototyping.notificationservice.domain.usecase.InputOutputUseCaseHandler
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.creation.NotificationTemplateCreationInput
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.creation.NotificationTemplateCreationOutput
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.retrieval.NotificationTemplatesRetrievalInput
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.update.NotificationTemplateUpdateInput
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/v1/notification-template")
internal class NotificationTemplateRestController(
    private val notificationTemplateCreationUseCaseHandler:
        InputOutputUseCaseHandler<NotificationTemplateCreationInput, NotificationTemplateCreationOutput>,
    private val notificationTemplatesRetrievalUseCaseHandler:
        InputOutputUseCaseHandler<NotificationTemplatesRetrievalInput, List<NotificationTemplate>>,
    private val notificationTemplateRetrievalUseCaseHandler:
        InputOutputUseCaseHandler<Int, NotificationTemplate>,
    private val notificationTemplateUpdateUseCaseHandler:
        InputOutputUseCaseHandler<NotificationTemplateUpdateInput, NotificationTemplate>
) {

    @PostMapping
    internal suspend fun createNotificationTemplate(@RequestBody body: NotificationTemplateCreationInput): ResponseEntity<Any?>
        = notificationTemplateCreationUseCaseHandler.handle(body)
            .let { ResponseEntity.created(URI.create("/api/v1/notification-template/${it.id}")).build() }

    @GetMapping
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
    internal suspend fun getNotificationTemplate(@PathVariable id: Int): ResponseEntity<NotificationTemplate> =
        ResponseEntity.ok(
            notificationTemplateRetrievalUseCaseHandler.handle(id)
        )

    @PatchMapping("/{id}")
    internal suspend fun updateContent(@PathVariable id: Int, @RequestBody body: NotificationTemplateUpdateRequest) =
        ResponseEntity.ok(
            notificationTemplateUpdateUseCaseHandler.handle(NotificationTemplateUpdateInput(
                id = id,
                content = body.content
            ))
        )
}