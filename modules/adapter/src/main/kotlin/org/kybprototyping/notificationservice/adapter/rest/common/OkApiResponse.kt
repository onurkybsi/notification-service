package org.kybprototyping.notificationservice.adapter.rest.common

import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType

@ApiResponse(responseCode = "200")
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class OkApiResponse(
    val description: String = "Successful retrieval",
    val content: Array<Content> = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)],
)
