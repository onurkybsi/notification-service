package org.kybprototyping.notificatin_service.domain.interfaces

/**
 * Represents the use cases with an input and an output.
 */
interface InputOutputUseCaseHandler<I, O> {

    /**
     * Handles the use case with given input and returns the output.
     *
     * @param input input of the use case
     * @return output of the use case
     */
    fun handle(input: I): O

}