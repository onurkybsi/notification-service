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

    /**
     * Creates a new template and returns its ID.
     *
     * @param channel notification channel
     * @param type type of the notification
     * @param language language of the notification
     * @param subject subject of the notification
     * @param content content of the notification
     * @return ID of the created template or **null** if the template with
     *  the same [channel], [type], [language] is already created, or,
     *  [UnexpectedFailure] if something went unexpectedly wrong
     */
    suspend fun create(
        channel: NotificationChannel,
        type: NotificationType,
        language: NotificationLanguage,
        subject: String,
        content: String,
    ): Either<UnexpectedFailure, Int?>

    /**
     * Deletes a notification template from the data repository by given ID.
     *
     * @param id notification template ID
     * @return [DeletionFailure] if something went wrong during deletion
     */
    suspend fun delete(id: Int): Either<DeletionFailure, Unit>

    /**
     * Update the notification template's subject and content by given ID.
     *
     * @param id ID of the template to update
     * @param subjectToSet updated subject, **if non-null**, if it's given as **null** no update will be made
     * @param contentToSet updated content, **if non-null**, if it's given as **null** no update will be made
     * @return [UpdateFailure] if something went wrong during deletion
     */
    suspend fun update(
        id: Int,
        subjectToSet: String?,
        contentToSet: String?,
    ): Either<UpdateFailure, Unit>

    /**
     * Failure that might occur during [delete] execution.
     */
    sealed class DeletionFailure {
        /**
         * Failure indicates that the template to delete doesn't exist.
         */
        data object DataNotFoundFailure : DeletionFailure()

        /**
         * Failure indicates that the deletion has unexpectedly failed.
         */
        data class UnexpectedFailure(val cause: Throwable) : DeletionFailure()
    }

    /**
     * Failure that might occur during [update] execution.
     */
    sealed class UpdateFailure {
        /**
         * Failure indicates that the template to update doesn't exist.
         */
        data object DataNotFoundFailure : UpdateFailure()

        /**
         * Failure indicates that the update has unexpectedly failed.
         */
        data class UnexpectedFailure(val cause: Throwable) : UpdateFailure()
    }
}
