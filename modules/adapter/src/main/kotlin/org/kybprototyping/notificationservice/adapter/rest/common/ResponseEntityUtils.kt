package org.kybprototyping.notificationservice.adapter.rest.common

import org.kybprototying.notificationservice.common.DataConflictFailure
import org.kybprototying.notificationservice.common.DataInvalidityFailure
import org.kybprototying.notificationservice.common.DataNotFoundFailure
import org.kybprototying.notificationservice.common.Failure
import org.kybprototying.notificationservice.common.UnexpectedFailure
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity

internal object ResponseEntityUtils {
    internal fun Failure.toResponseEntity(): ResponseEntity<ProblemDetail> =
        when (this) {
            is DataInvalidityFailure ->
                ResponseEntity
                    .of(
                        problemDetail(
                            status = HttpStatus.BAD_REQUEST,
                            properties = validationResult?.let { mapOf("validationResult" to this.validationResult!!) }
                        )
                    )
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
