package org.kybprototyping.notificationservice.adapter.repository.notificationtemplate

import org.kybprototyping.notificationservice.domain.model.NotificationLanguage as DomainNotificationLanguage

internal enum class NotificationLanguage {
    EN,
    ;

    internal companion object {
        internal fun NotificationLanguage.toDomain() =
            when (this) {
                EN -> DomainNotificationLanguage.EN
            }

        internal fun from(domain: DomainNotificationLanguage) =
            when (domain) {
                DomainNotificationLanguage.EN -> EN
            }
    }
}
