package org.kybprototyping.notificationservice.adapter.rest.notificationtemplate

import org.kybprototyping.notificationservice.domain.model.NotificationType as DomainNotificationType

internal enum class NotificationType {
    WELCOME,
    PASSWORD_RESET;

    internal companion object {
        internal fun NotificationType.toDomain() =
            when(this) {
                WELCOME -> DomainNotificationType.WELCOME
                PASSWORD_RESET -> DomainNotificationType.PASSWORD_RESET
            }
    }
}