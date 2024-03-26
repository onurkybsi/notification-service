package org.kybprototyping.notificatin_service.domain.interfaces

/**
 * Represents the use cases with an input and no output.
 */
interface InputOnlyUseCaseHandler<I> {

    /**
     * Handles the use case with given input.
     *
     * @param input input of the use case
     */
    fun handle(input: I)

}