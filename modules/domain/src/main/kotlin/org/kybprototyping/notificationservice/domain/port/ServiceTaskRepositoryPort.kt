package org.kybprototyping.notificationservice.domain.port

import arrow.core.Either
import org.kybprototying.notificationservice.common.DataConflictFailure
import org.kybprototying.notificationservice.common.Failure
import org.kybprototying.notificationservice.common.UnexpectedFailure
import org.kybprototyping.notificationservice.domain.model.ServiceTask

/**
 * Represents the API that provides access to [ServiceTask] data repository.
 */
interface ServiceTaskRepositoryPort {
    /**
     * Insert the given service task into the underlying data repository.
     *
     * @param task service task to be inserted
     * @return [DataConflictFailure] if a task with same external ID already inserted,
     * [UnexpectedFailure] if something went unexpectedly wrong
     */
    suspend fun insert(task: ServiceTask): Either<Failure, Unit>
}
