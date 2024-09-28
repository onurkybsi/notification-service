package org.kybprototyping.notificationservice.adapter.rest.common

import org.apache.logging.log4j.kotlin.Logging
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ProblemDetail.forStatusAndDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
internal class GlobalRestExceptionHandler : Logging {

    @ExceptionHandler(value = [Throwable::class])
    internal fun unexpectedExceptionHandler(throwable: Throwable): ResponseEntity<ProblemDetail> =
        ResponseEntity.of(forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred!")).build<ProblemDetail>()
            .also {
                logger.error("An unexpected error occurred!", throwable)
            }

}