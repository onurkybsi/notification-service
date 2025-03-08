package org.kybprototyping.notificationservice.adapter.rest.notification

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.kybprototyping.notificationservice.adapter.rest.common.ConflictApiResponse
import org.kybprototyping.notificationservice.adapter.rest.common.InternalServerErrorApiResponse
import org.kybprototyping.notificationservice.adapter.rest.common.InvalidRequestApiResponse
import org.kybprototyping.notificationservice.adapter.rest.common.ResponseEntityUtils.toResponseEntity
import org.kybprototyping.notificationservice.adapter.rest.notification.SendEmailRequest.Companion.toEmailSendingInput
import org.kybprototyping.notificationservice.domain.common.UseCaseHandler
import org.kybprototyping.notificationservice.domain.usecase.notification.SendEmailInput
import org.kybprototyping.notificationservice.domain.usecase.notification.SendEmailOutput
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/notification")
@Tag(
    name = "notification",
    description = "Notification API v1"
)
internal class NotificationRestController(private val sendEmailUseCase: UseCaseHandler<SendEmailInput, SendEmailOutput>) {
    @PostMapping("/email")
    @Operation(summary = "Submits a send email task.")
    @ApiResponse(
        responseCode = "202",
        description = "Successful submission",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = SendEmailResponse::class),
            ),
        ]
    )
    @InvalidRequestApiResponse
    @ConflictApiResponse(description = "A send email task with given idempotency key already submitted")
    @InternalServerErrorApiResponse
    internal suspend fun sendEmail(
        @RequestHeader(name = "X-Idempotency-Key", required = false) externalId: UUID? = null,
        @RequestBody body: SendEmailRequest,
    ): ResponseEntity<*> =
        sendEmailUseCase.handle(body.toEmailSendingInput(externalId))
            .fold(
                ifLeft = { it.toResponseEntity() },
                ifRight = { ResponseEntity.accepted().body(SendEmailResponse(it.externalId)) }
            )
}
