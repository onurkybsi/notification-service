package org.kybprototyping.notificationservice.adapter.repository.notificationtemplate

import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.NotificationChannel.Companion.toDomain
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.NotificationLanguage.Companion.toDomain
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.NotificationType.Companion.toDomain
import java.time.LocalDateTime
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate as DomainNotificationTemplate
import java.time.OffsetDateTime
import java.time.ZoneOffset

internal data class NotificationTemplate(
    val id: Int?,
    val channel: NotificationChannel,
    val type: NotificationType,
    val language: NotificationLanguage,
    val subject: String,
    val content: String,
    val modifiedBy: String? = null,
    val modifiedAt: LocalDateTime,
    val createdBy: String? = null,
    val createdAt: LocalDateTime
) {
    internal companion object {
        internal fun NotificationTemplate.toDomain() =
            DomainNotificationTemplate(
                id = this.id!!,
                channel = this.channel.toDomain(),
                type = this.type.toDomain(),
                language = this.language.toDomain(),
                subject = this.subject,
                content = this.content,
                modifiedBy = this.modifiedBy,
                modifiedAt = OffsetDateTime.of(this.modifiedAt, ZoneOffset.UTC),
                createdBy = this.createdBy,
                createdAt = OffsetDateTime.of(this.createdAt, ZoneOffset.UTC)
            )

        internal fun from(domain: DomainNotificationTemplate) =
            NotificationTemplate(
                id = domain.id,
                channel = NotificationChannel.from(domain.channel),
                type = NotificationType.from(domain.type),
                language = NotificationLanguage.from(domain.language),
                subject = domain.subject,
                content = domain.content,
                modifiedBy = domain.modifiedBy,
                modifiedAt = domain.modifiedAt.toLocalDateTime(),
                createdBy = domain.createdBy,
                createdAt = domain.createdAt.toLocalDateTime()
            )
    }
}