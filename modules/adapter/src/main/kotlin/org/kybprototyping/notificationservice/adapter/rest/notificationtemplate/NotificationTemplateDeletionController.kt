package org.kybprototyping.notificationservice.adapter.rest.notificationtemplate

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.kybprototyping.notificationservice.adapter.rest.common.InternalServerErrorApiResponse
import org.kybprototyping.notificationservice.adapter.rest.common.NotFoundApiResponse
import org.kybprototyping.notificationservice.adapter.rest.common.ResponseEntityUtils.toResponseEntity
import org.kybprototyping.notificationservice.domain.common.UseCaseHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/notification-template")
@Tag(name = "notification-template")
internal class NotificationTemplateDeletionController(private val useCaseHandler: UseCaseHandler<Int, Unit>) {
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes an existing notification template.")
    @ApiResponse(
        responseCode = "204",
        description = "Successful deletion",
    )
    @NotFoundApiResponse(description = "Template to delete doesn't exist.")
    @InternalServerErrorApiResponse
    internal suspend fun deleteNotificationTemplate(
        @PathVariable id: Int,
    ): ResponseEntity<*> =
        useCaseHandler.handle(id)
            .fold(
                ifLeft = { it.toResponseEntity() },
                ifRight = { ResponseEntity.noContent().build() },
            )
}
