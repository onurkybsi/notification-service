package org.kybprototyping.notificationservice.adapter.rest

import org.kybprototyping.notificationservice.domain.model.*
import org.kybprototyping.notificationservice.domain.usecase.InputOutputUseCaseHandler
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplatecreation.NotificationTemplateCreationInput
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplatecreation.NotificationTemplateCreationOutput
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplatesretrieval.NotificationTemplatesRetrievalInput
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/v1/notification-template")
internal class NotificationTemplateRestController(
    private val notificationTemplateCreationUseCaseHandler:
        InputOutputUseCaseHandler<NotificationTemplateCreationInput, NotificationTemplateCreationOutput>,
    private val notificationTemplatesRetrievalUseCaseHandler:
        InputOutputUseCaseHandler<NotificationTemplatesRetrievalInput, List<NotificationTemplate>>
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
        ResponseEntity.ok(notificationTemplatesRetrievalUseCaseHandler.handle(
            NotificationTemplatesRetrievalInput(
            channel = channel,
            type = type,
            language = language
        )
        ))

}