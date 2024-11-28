package org.kybprototyping.notificationservice.adapter.rest.common

import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail

@ApiResponse(
    responseCode = "400",
    content = [
        Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = ProblemDetail::class),
        ),
    ],
)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class InvalidRequestApiResponse(
    val description: String = "Invalid request",
)
