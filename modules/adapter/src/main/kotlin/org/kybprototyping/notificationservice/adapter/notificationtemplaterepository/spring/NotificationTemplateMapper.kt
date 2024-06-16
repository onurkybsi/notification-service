package org.kybprototyping.notificationservice.adapter.notificationtemplaterepository.spring

import io.r2dbc.spi.Readable
import org.kybprototyping.notificationservice.domain.model.NotificationChannel
import org.kybprototyping.notificationservice.domain.model.NotificationLanguage
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate
import org.kybprototyping.notificationservice.domain.model.NotificationType
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
internal class NotificationTemplateMapper {

    internal fun toDto(row: Readable) =
        NotificationTemplate(
            id = row.get("id") as Int,
            channel = toDto(row.get("channel") as NotificationChannelEntity),
            type = toDto(row.get("type") as NotificationTypeEntity),
            language = toDto(row.get("language") as NotificationLanguageEntity),
            subject = row.get("subject") as String,
            content = row.get("content") as String,
            modifiedBy = row.get("modified_by") as String?,
            modificationDate = OffsetDateTime.of(row.get("modification_date") as LocalDateTime, ZoneOffset.UTC),
            createdBy = row.get("created_by") as String?,
            creationDate = OffsetDateTime.of(row.get("creation_date") as LocalDateTime, ZoneOffset.UTC),
        )

    internal fun toDto(channelEntity: NotificationChannelEntity) =
        when(channelEntity) {
            NotificationChannelEntity.EMAIL -> NotificationChannel.EMAIL
        }

    internal fun toDto(typeEntity: NotificationTypeEntity) =
        when(typeEntity) {
            NotificationTypeEntity.WELCOME -> NotificationType.WELCOME
            NotificationTypeEntity.PASSWORD_RESET -> NotificationType.PASSWORD_RESET
        }

    internal fun toDto(languageEntity: NotificationLanguageEntity) =
        when(languageEntity) {
            NotificationLanguageEntity.EN -> NotificationLanguage.EN
        }

    internal fun toEntity(channelDto: NotificationChannel) =
        when(channelDto) {
            NotificationChannel.EMAIL -> NotificationChannelEntity.EMAIL
        }

    internal fun toEntity(typeDto: NotificationType) =
        when(typeDto) {
            NotificationType.WELCOME -> NotificationTypeEntity.WELCOME
            NotificationType.PASSWORD_RESET -> NotificationTypeEntity.PASSWORD_RESET
        }

    internal fun toEntity(languageDto: NotificationLanguage) =
        when(languageDto) {
            NotificationLanguage.EN -> NotificationLanguageEntity.EN
        }

}