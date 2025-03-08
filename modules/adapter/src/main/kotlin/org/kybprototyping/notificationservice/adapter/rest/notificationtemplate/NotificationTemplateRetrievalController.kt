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
import org.kybprototyping.notificationservice.adapter.rest.notificationtemplate.NotificationChannel.Companion.toDomain
import org.kybprototyping.notificationservice.adapter.rest.notificationtemplate.NotificationLanguage.Companion.toDomain
import org.kybprototyping.notificationservice.adapter.rest.notificationtemplate.NotificationType.Companion.toDomain
import org.kybprototyping.notificationservice.domain.common.UseCaseHandler
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplatesRetrievalInput
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate as DomainNotificationTemplate

@RestController
@RequestMapping("/api/v1/notification-template")
@Tag(
    name = "notification-template",
    description = "Notification Template API v1",
)
internal class NotificationTemplateRetrievalController(
    private val notificationTemplatesRetrievalUseCase: UseCaseHandler<NotificationTemplatesRetrievalInput, List<DomainNotificationTemplate>>,
    private val notificationTemplateRetrievalUseCase: UseCaseHandler<Int, DomainNotificationTemplate>,
) {
    // TODO: Pagination!
    // TODO: Return Flux!
    @GetMapping
    @Operation(summary = "Returns the notification templates by given values.")
    @OkApiResponse(
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = ArraySchema(schema = Schema(implementation = NotificationTemplate::class)),
            ),
        ],
    )
    @InternalServerErrorApiResponse
    internal suspend fun getNotificationTemplates(
        @RequestParam(required = false) channel: NotificationChannel?,
        @RequestParam(required = false) type: NotificationType?,
        @RequestParam(required = false) language: NotificationLanguage?,
    ): ResponseEntity<*> =
        notificationTemplatesRetrievalUseCase.handle(
            NotificationTemplatesRetrievalInput(
                channel = channel?.toDomain(),
                type = type?.toDomain(),
                language = language?.toDomain(),
            ),
        ).fold(
            ifLeft = { it.toResponseEntity() },
            ifRight = { ResponseEntity.ok(it) },
        )

    @GetMapping("/{id}")
    @Operation(summary = "Returns the notification template by given ID.")
    @OkApiResponse(
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = NotificationTemplate::class),
            ),
        ],
    )
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    internal suspend fun getNotificationTemplate(
        @PathVariable id: Int,
    ): ResponseEntity<*> =
        notificationTemplateRetrievalUseCase.handle(id)
            .fold(
                ifLeft = { it.toResponseEntity() },
                ifRight = { ResponseEntity.ok(it) },
            )
}
