package org.kybprototyping.notificationservice.adapter.rest

import org.kybprototyping.notificationservice.domain.usecase.UseCaseException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
internal class GlobalRestExceptionHandler {

    @ExceptionHandler(value = [UseCaseException::class])
    internal fun useCaseExceptionHandler(exception: UseCaseException): ResponseEntity<ProblemDetail> =
        if (exception.dueToDataInvalidity) {
            ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.message)).build()
        } else if (exception.dueToNonExistentData) {
        ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.message)).build()
        } else {
            ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred!")).build()
        }

}