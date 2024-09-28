package org.kybprototyping.notificationservice.adapter.repository.notificationtemplate

import org.kybprototyping.notificationservice.domain.model.NotificationChannel as DomainNotificationChannel

internal enum class NotificationChannel {
    EMAIL;

    internal companion object {
        internal fun NotificationChannel.toDomain() =
            when(this) {
                EMAIL -> DomainNotificationChannel.EMAIL
            }

        internal fun from(domain: DomainNotificationChannel) =
            when(domain) {
                DomainNotificationChannel.EMAIL -> EMAIL
            }
    }
}