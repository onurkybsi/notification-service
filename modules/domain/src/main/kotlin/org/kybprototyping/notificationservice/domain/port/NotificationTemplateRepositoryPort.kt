package org.kybprototyping.notificationservice.domain.port

import org.kybprototyping.notificationservice.domain.model.*

/**
 * Represents the API which provides access to [NotificationTemplate] datasource.
 */
interface NotificationTemplateRepositoryPort {

    /**
     * Creates a [NotificationTemplate] in the underlying datasource.
     *
     * @param request required parameters for the creation
     * @return created notification template ID
     */
    suspend fun create(request: NotificationTemplateCreationRequest): Int

    /**
     * Returns the notification templates with given filtering values.
     *
     * @param channel channel of the notification
     * @param type type of the notification
     * @param language language of the notification
     * @return notification templates with given filtering values
     */
    suspend fun getListBy(channel: NotificationChannel?, type: NotificationType?, language: NotificationLanguage?): List<NotificationTemplate>

    /**
     * Returns the notification template with given filtering values.
     *
     * @param channel channel of the notification
     * @param type type of the notification
     * @param language language of the notification
     * @return notification template with given filtering values if there is one, otherwise _null_
     */
    suspend fun getOneBy(channel: NotificationChannel, type: NotificationType, language: NotificationLanguage): NotificationTemplate?

    /**
     * Returns the notification template with given ID.
     *
     * @param id notification template ID
     * @return notification template with given ID if there is one, otherwise _null_
     */
    suspend fun getById(id: Int): NotificationTemplate?

}
