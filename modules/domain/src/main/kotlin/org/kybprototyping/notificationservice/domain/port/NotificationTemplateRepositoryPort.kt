package org.kybprototyping.notificationservice.domain.port

import arrow.core.Either
import org.kybprototyping.notificationservice.domain.common.UnexpectedFailure
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate

/**
 * Represents the API that provides access to [NotificationTemplate] data repository.
 */
interface NotificationTemplateRepositoryPort {

    /**
     * Returns the [NotificationTemplate] with given ID.
     *
     * @param id notification template ID
     * @return template with given [id] or *null* if no template found by given [id],
     *  or, [UnexpectedFailure] if something went unexpectedly wrong during fetching the template
     */
    suspend fun findById(id: Int): Either<UnexpectedFailure, NotificationTemplate?>

}