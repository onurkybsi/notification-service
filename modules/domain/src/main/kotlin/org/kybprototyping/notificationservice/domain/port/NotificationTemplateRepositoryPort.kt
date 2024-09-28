package org.kybprototyping.notificationservice.domain.port

import arrow.core.Either
import org.kybprototyping.notificationservice.domain.common.UnexpectedFailure
import org.kybprototyping.notificationservice.domain.model.NotificationChannel
import org.kybprototyping.notificationservice.domain.model.NotificationLanguage
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate
import org.kybprototyping.notificationservice.domain.model.NotificationType

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

    /**
     * Returns the [NotificationTemplate] entities that have the given values.
     *
     * @param channel notification channel
     * @param type type of the notification
     * @param language language of the notification
     * @return templates that have the given values, or,
     *  [UnexpectedFailure] if something went unexpectedly wrong during fetching the templates
     */
    suspend fun findBy(
        channel: NotificationChannel?,
        type: NotificationType?,
        language: NotificationLanguage?,
    ): Either<UnexpectedFailure, List<NotificationTemplate>>

}