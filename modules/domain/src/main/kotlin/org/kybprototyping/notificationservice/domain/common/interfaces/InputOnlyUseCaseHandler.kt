package org.kybprototyping.notificationservice.domain.common.interfaces

/**
 * Represents the use cases with an input and no output.
 */
interface InputOnlyUseCaseHandler<in I> {

    /**
     * Handles the use case with given input.
     *
     * @param input input of the use case
     */
    suspend fun handle(input: I)

}