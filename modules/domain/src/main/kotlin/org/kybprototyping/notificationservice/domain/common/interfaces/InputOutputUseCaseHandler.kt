package org.kybprototyping.notificationservice.domain.common.interfaces

/**
 * Represents the use cases with an input and output.
 */
interface InputOutputUseCaseHandler<in I, out  O> {

    /**
     * Handles the use case with given input.
     *
     * @param input input of the use case
     * @return output of the use case
     */
    suspend fun handle(input: I): O

}