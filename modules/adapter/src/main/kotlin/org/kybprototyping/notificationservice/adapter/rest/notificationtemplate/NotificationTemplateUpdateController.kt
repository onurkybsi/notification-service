package org.kybprototyping.notificationservice.adapter.rest.notificationtemplate

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.kybprototyping.notificationservice.adapter.rest.common.InternalServerErrorApiResponse
import org.kybprototyping.notificationservice.adapter.rest.common.NotFoundApiResponse
import org.kybprototyping.notificationservice.adapter.rest.common.ResponseEntityUtils.toResponseEntity
import org.kybprototyping.notificationservice.domain.common.UseCaseHandler
import org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.NotificationTemplateUpdateInput
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/notification-template")
@Tag(name = "notification-template")
internal class NotificationTemplateUpdateController(private val useCase: UseCaseHandler<NotificationTemplateUpdateInput, Unit>) {
    @PatchMapping("/{id}")
    @Operation(
        summary = """
            Updates the notification template with given ID.
            See, all the values in the body are nullable, so the ones that are left as null won't be updated.
        """,
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successful update",
        content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)],
    )
    @NotFoundApiResponse("Template to update doesn't exist.")
    @InternalServerErrorApiResponse
    internal suspend fun updateNotificationTemplate(
        @PathVariable id: Int,
        @RequestBody body: NotificationTemplateUpdateRequest,
    ): ResponseEntity<*> =
        useCase.handle(
            NotificationTemplateUpdateInput(
                id = id,
                subject = body.subject,
                content = body.content,
            ),
        ).fold(
            ifLeft = { it.toResponseEntity() },
            ifRight = { ResponseEntity.ok().build() },
        )
}
