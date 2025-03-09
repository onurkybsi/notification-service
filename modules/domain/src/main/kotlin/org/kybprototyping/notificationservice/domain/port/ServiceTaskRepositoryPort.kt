package org.kybprototyping.notificationservice.domain.port

import arrow.core.Either
import com.fasterxml.jackson.core.TreeNode
import org.kybprototying.notificationservice.common.DataConflictFailure
import org.kybprototying.notificationservice.common.Failure
import org.kybprototying.notificationservice.common.UnexpectedFailure
import org.kybprototyping.notificationservice.domain.model.ServiceTask
import org.kybprototyping.notificationservice.domain.model.ServiceTaskStatus
import java.time.OffsetDateTime
import java.util.UUID

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

    /**
     * Updates the tasks with given values by given values.
     *
     * The tasks to be updated must have one of the statuses given **and**
     * execution scheduled date time less than or equal to given [executionScheduledAt] or as *null*.
     *
     * @param statuses statuses to be updated from
     * @param executionScheduledAt execution scheduled date to be updated from (less, equal or *null*)
     * @param statusToSet status to be updated to
     * @param executionScheduledAtToSet execution scheduled date to be updated to
     * @param executionStartedAtToSet execution start date time to be updated to
     * @return updated tasks, [UnexpectedFailure] if something went unexpectedly wrong
     */
    suspend fun updateBy(
        statuses: List<ServiceTaskStatus>,
        executionScheduledAt: OffsetDateTime,
        statusToSet: ServiceTaskStatus,
        executionScheduledAtToSet: OffsetDateTime?,
        executionStartedAtToSet: OffsetDateTime,
    ): Either<Failure, List<ServiceTask>> // TODO: Maybe, limit?

    /**
     * Updates the task with given ID by given values.
     *
     * @param id task ID
     * @param statusToSet status to be updated to
     * @param executionCountToSet execution count to be updated to
     * @param executionStartedAtToSet execution start date time to be updated to
     * @param executionScheduledAtToSet execution scheduled date to be updated to
     * @param contextToSet context to be updated to
     * @param messageToSet message to be updated to
     * @return [UnexpectedFailure] if something went unexpectedly wrong
     */
    suspend fun updateBy(
        id: UUID,
        statusToSet: ServiceTaskStatus,
        executionCountToSet: Int,
        executionStartedAtToSet: OffsetDateTime?,
        executionScheduledAtToSet: OffsetDateTime?,
        contextToSet: TreeNode?,
        messageToSet: String?,
    ): Either<Failure, Unit>
}
