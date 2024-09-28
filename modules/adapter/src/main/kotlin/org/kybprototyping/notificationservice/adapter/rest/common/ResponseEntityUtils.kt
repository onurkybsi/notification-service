package org.kybprototyping.notificationservice.adapter.rest.common

import org.kybprototyping.notificationservice.domain.common.DataInvalidityFailure
import org.kybprototyping.notificationservice.domain.common.Failure
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity

internal object ResponseEntityUtils {

    internal fun Failure.toResponseEntity(): ResponseEntity<ProblemDetail> =
        when(this) {
            is DataInvalidityFailure -> ResponseEntity
                .badRequest()
                .body(
                    problemDetail(
                        HttpStatus.BAD_REQUEST, "Invalid request.", mapOf("validationResult" to this.validationResult)
                    )
                )
        }

    private fun problemDetail(
        status: HttpStatus,
        detail: String,
        properties: Map<String, Any>,
    ) =
        ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(status.value()), detail).also {
            it.properties = properties
        }

}