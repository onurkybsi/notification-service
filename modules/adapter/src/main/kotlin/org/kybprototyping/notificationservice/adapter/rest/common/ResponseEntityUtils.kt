package org.kybprototyping.notificationservice.adapter.rest.common

import org.kybprototyping.notificationservice.domain.common.DataConflictFailure
import org.kybprototyping.notificationservice.domain.common.DataInvalidityFailure
import org.kybprototyping.notificationservice.domain.common.DataNotFoundFailure
import org.kybprototyping.notificationservice.domain.common.Failure
import org.kybprototyping.notificationservice.domain.common.UnexpectedFailure
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity

internal object ResponseEntityUtils {
    internal fun Failure.toResponseEntity(): ResponseEntity<ProblemDetail> =
        when (this) {
            is DataInvalidityFailure ->
                ResponseEntity
                    .of(problemDetail(HttpStatus.BAD_REQUEST, properties = mapOf("validationResult" to this.validationResult)))
                    .build()
            is DataNotFoundFailure -> ResponseEntity.of(problemDetail(HttpStatus.NOT_FOUND, detail = this.message)).build()
            is DataConflictFailure -> ResponseEntity.of(problemDetail(HttpStatus.CONFLICT, detail = this.message)).build()
            is UnexpectedFailure ->
                ResponseEntity.of(
                    problemDetail(
                        status = HttpStatus.INTERNAL_SERVER_ERROR,
                        detail = this.message,
                        properties = mapOf("isTemporary" to this.isTemporary),
                    ),
                ).build()
        }

    private fun problemDetail(
        status: HttpStatus,
        detail: String? = null,
        properties: Map<String, Any>? = null,
    ) = ProblemDetail.forStatus(status.value()).also {
        it.detail = detail ?: status.reasonPhrase
        it.properties = properties
    }
}
