package org.kybprototyping.notificationservice.adapter.repository.notificationtemplate

import org.kybprototyping.notificationservice.domain.model.NotificationType as DomainNotificationType

internal enum class NotificationType {
    WELCOME,
    PASSWORD_RESET,
    ;

    internal companion object {
        internal fun NotificationType.toDomain() =
            when (this) {
                WELCOME -> DomainNotificationType.WELCOME
                PASSWORD_RESET -> DomainNotificationType.PASSWORD_RESET
            }

        internal fun from(domain: DomainNotificationType) =
            when (domain) {
                DomainNotificationType.WELCOME -> WELCOME
                DomainNotificationType.PASSWORD_RESET -> PASSWORD_RESET
            }
    }
}
