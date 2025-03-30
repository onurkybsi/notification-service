package org.kybprototyping.notificationservice.domain.port

import arrow.core.Either
import org.kybprototying.notificationservice.common.Failure
import org.kybprototying.notificationservice.common.UnexpectedFailure
import org.kybprototyping.notificationservice.domain.model.ServiceTaskType
import java.util.UUID

/**
 * Represents the API that publishes service task outputs.
 */
fun interface ServiceTaskPublisherPort {
    /**
     * Publishes the output of completed service task to the subscribers.
     *
     * @param type task type
     * @param externalId ID in the subscriber side
     * @param output output of the task completed
     * @return [UnexpectedFailure] if something went unexpectedly wrong
     */
    suspend fun execute(type: ServiceTaskType, externalId: UUID, output: ByteArray): Either<Failure, Unit>
}
