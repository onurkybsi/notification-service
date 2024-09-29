package org.kybprototyping.notificationservice.adapter.rest.notificationtemplate

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.kybprototyping.notificationservice.adapter.rest.common.ConflictApiResponse
import org.kybprototyping.notificationservice.adapter.rest.common.InvalidRequestResponse
import org.kybprototyping.notificationservice.adapter.rest.common.ResponseEntityUtils.toResponseEntity
import org.kybprototyping.notificationservice.adapter.rest.notificationtemplate.NotificationTemplateCreationRequest.Companion.toDomain
import org.kybprototyping.notificationservice.domain.common.UseCaseHandler
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplateCreationInput
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/v1/notification-template")
@Tag(name = "notification-template")
internal class NotificationTemplateCreationController(
    private val useCaseHandler: UseCaseHandler<NotificationTemplateCreationInput, Int>
) {
    @PostMapping
    @Operation(summary = "Creates a notification template.")
    @ApiResponse(
        responseCode = "201",
        description = "Successful creation"
    )
    @InvalidRequestResponse
    @ConflictApiResponse(description = "Template with the same channel, type and language is already created.")
    internal suspend fun createNotificationTemplate(@RequestBody body: NotificationTemplateCreationRequest): ResponseEntity<*> =
        useCaseHandler.handle(body.toDomain())
            .fold(
                ifLeft = { it.toResponseEntity() },
                ifRight = { ResponseEntity.created(URI.create("/api/v1/notification-template/${it}")).build() }
            )
}