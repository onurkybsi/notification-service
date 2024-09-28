package org.kybprototyping.notificationservice.adapter.rest.notificationtemplate

import org.kybprototyping.notificationservice.domain.model.NotificationChannel as DomainNotificationChannel

internal enum class NotificationChannel {
    EMAIL;

    internal companion object {
        internal fun NotificationChannel.toDomain() =
            when(this) {
                EMAIL -> DomainNotificationChannel.EMAIL
            }
    }
}