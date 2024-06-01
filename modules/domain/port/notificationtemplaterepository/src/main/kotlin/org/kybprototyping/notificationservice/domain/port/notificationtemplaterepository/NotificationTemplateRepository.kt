package org.kybprototyping.notificationservice.domain.port.notificationtemplaterepository

import org.kybprototyping.notificationservice.domain.common.exception.UseCaseException
import org.kybprototyping.notificationservice.domain.model.*

/**
 * Represents the API which provides access to [NotificationTemplate] datasource.
 */
interface NotificationTemplateRepository {

    /**
     * Creates a [NotificationTemplate] in the underlying datasource.
     *
     * @param request required parameters for the creation
     * @return created notification template ID
     */
    suspend fun create(request: NotificationTemplateCreationRequest): Long

    /**
     * Returns the notification template with given filtering values.
     *
     * @param channel channel of the notification
     * @param type type of the notification
     * @param language language of the notification
     * @return the notification template with given filtering values
     * @throws UseCaseException when no template exists with given filtering values
     */
    suspend fun get(channel: NotificationChannel, type: NotificationType, language: NotificationLanguage): NotificationTemplate

}
