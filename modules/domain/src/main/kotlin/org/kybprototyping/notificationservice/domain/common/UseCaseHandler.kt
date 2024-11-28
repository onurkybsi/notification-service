package org.kybprototyping.notificationservice.domain.common

import arrow.core.Either

/**
 * Represents the _Notification Service_ use case implementations.
 */
interface UseCaseHandler<in I, out O> {
    /**
     * Handles the use case with given input.
     *
     * @param input input of the use case
     * @return output of the use case or [Failure] if something went wrong
     */
    suspend fun handle(input: I): Either<Failure, O>
}
