package org.kybprototyping.notificationservice.domain.scheduled.servicetask

import arrow.core.Either
import org.kybprototying.notificationservice.common.Failure
import org.kybprototyping.notificationservice.domain.model.ServiceTask

/**
 * Represents the API that executes a particular type of service task.
 */
internal interface ServiceTaskExecutor {
    /**
     * Executes a particular type of service task.
     */
    suspend fun execute(task: ServiceTask): Either<Failure, Unit>
}
