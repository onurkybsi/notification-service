package org.kybprototyping.notificationservice.adapter.repository.notificationtemplate

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import org.kybprototying.notificationservice.common.DataNotFoundFailure
import org.kybprototying.notificationservice.common.TimeUtils
import org.kybprototying.notificationservice.common.UnexpectedFailure
import org.kybprototying.notificationservice.common.runExceptionCatching
import org.kybprototyping.notificationservice.adapter.repository.common.TransactionAwareDSLContextProxy
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.Tables.NOTIFICATION_TEMPLATE
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.tables.records.NotificationTemplateRecord
import org.kybprototyping.notificationservice.domain.model.NotificationChannel
import org.kybprototyping.notificationservice.domain.model.NotificationLanguage
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate
import org.kybprototyping.notificationservice.domain.model.NotificationType
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.enums.NotificationChannel as RecordNotificationChannel
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.enums.NotificationLanguage as RecordNotificationLanguage
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.enums.NotificationType as RecordNotificationType

@Component
@ConditionalOnProperty(
    value = ["ports.notification-template-repository.impl"],
    havingValue = "jooq",
)
internal class JooqImpl(
    private val transactionAwareDSLContextProxy: TransactionAwareDSLContextProxy,
    private val timeUtils: TimeUtils,
) : NotificationTemplateRepositoryPort {
    override suspend fun findById(id: Int) =
        try {
            transactionAwareDSLContextProxy.dslContext()
                .selectFrom(NOTIFICATION_TEMPLATE)
                .where(NOTIFICATION_TEMPLATE.ID.eq(id))
                .awaitSingle()
                .toDomain()
                .right()
        } catch (e: NoSuchElementException) {
            null.right()
        } catch (e: Exception) {
            UnexpectedFailure(cause = e).left()
        }

    override suspend fun findBy(
        channel: NotificationChannel?,
        type: NotificationType?,
        language: NotificationLanguage?,
    ) = try {
        var query = transactionAwareDSLContextProxy.dslContext().selectFrom(NOTIFICATION_TEMPLATE).where()
        channel?.let { query = query.and(NOTIFICATION_TEMPLATE.CHANNEL.eq((toRecord(channel)))) }
        type?.let { query = query.and(NOTIFICATION_TEMPLATE.TYPE.eq(toRecord(type))) }
        language?.let { query = query.and(NOTIFICATION_TEMPLATE.LANGUAGE.eq(toRecord(language))) }

        Flux.from(query)
            .map { it.toDomain() }
            .collectList()
            .awaitSingle()
            .right()
    } catch (e: Exception) {
        UnexpectedFailure(cause = e).left()
    }

    override suspend fun findOneBy(
        channel: NotificationChannel,
        type: NotificationType,
        language: NotificationLanguage
    ) =
        runExceptionCatching {
            transactionAwareDSLContextProxy.dslContext()
                .selectFrom(NOTIFICATION_TEMPLATE)
                .where()
                .and(NOTIFICATION_TEMPLATE.CHANNEL.eq((toRecord(channel))))
                .and(NOTIFICATION_TEMPLATE.LANGUAGE.eq(toRecord(language)))
                .and(NOTIFICATION_TEMPLATE.TYPE.eq(toRecord(type)))
                .awaitFirstOrNull()
                ?.toDomain()
                ?.right()
                ?: DataNotFoundFailure("No template exists by given values: $channel & $type & $language").left()
        }

    override suspend fun create(
        channel: NotificationChannel,
        type: NotificationType,
        language: NotificationLanguage,
        subject: String,
        content: String,
    ) = try {
        transactionAwareDSLContextProxy.dslContext()
            .insertInto(NOTIFICATION_TEMPLATE)
            .set(
                NotificationTemplateRecord().also {
                    it.channel = toRecord(channel)
                    it.type = toRecord(type)
                    it.language = toRecord(language)
                    it.subject = subject
                    it.content = content
                    it.modifiedAt = timeUtils.nowAsLocalDateTime()
                    it.createdAt = timeUtils.nowAsLocalDateTime()
                },
            )
            .onDuplicateKeyIgnore()
            .returningResult(NOTIFICATION_TEMPLATE.ID)
            .awaitFirstOrNull()
            ?.get(0, Int::class.java)
            .right()
    } catch (e: Exception) {
        UnexpectedFailure(cause = e).left()
    }

    override suspend fun delete(id: Int) =
        try {
            transactionAwareDSLContextProxy.dslContext()
                .deleteFrom(NOTIFICATION_TEMPLATE)
                .where(NOTIFICATION_TEMPLATE.ID.eq(id))
                .awaitSingle()
                .let { rowsDeleted ->
                    if (rowsDeleted == 0) {
                        NotificationTemplateRepositoryPort.DeletionFailure.DataNotFoundFailure.left()
                    } else {
                        Unit.right()
                    }
                }
        } catch (e: Exception) {
            NotificationTemplateRepositoryPort.DeletionFailure.UnexpectedFailure(cause = e).left()
        }

    override suspend fun update(
        id: Int,
        subjectToSet: String?,
        contentToSet: String?,
    ): Either<NotificationTemplateRepositoryPort.UpdateFailure, Unit> {
        try {
            if (subjectToSet == null && contentToSet == null) {
                return Unit.right()
            }

            var statement =
                transactionAwareDSLContextProxy.dslContext()
                    .update(NOTIFICATION_TEMPLATE)
                    .set(NOTIFICATION_TEMPLATE.MODIFIED_AT, timeUtils.nowAsLocalDateTime())
            subjectToSet?.let { statement = statement.set(NOTIFICATION_TEMPLATE.SUBJECT, subjectToSet) }
            contentToSet?.let { statement = statement.set(NOTIFICATION_TEMPLATE.CONTENT, contentToSet) }
            statement.where(NOTIFICATION_TEMPLATE.ID.eq(id))

            return statement.awaitSingle().let { rowsUpdated ->
                if (rowsUpdated > 0) {
                    Unit.right()
                } else {
                    NotificationTemplateRepositoryPort.UpdateFailure.DataNotFoundFailure.left()
                }
            }
        } catch (e: Exception) {
            return NotificationTemplateRepositoryPort.UpdateFailure.UnexpectedFailure(cause = e).left()
        }
    }

    internal companion object {
        internal fun NotificationTemplateRecord.toDomain() =
            NotificationTemplate(
                id = this.id!!,
                channel = toDomain(this.channel),
                type = toDomain(this.type),
                language = toDomain(this.language),
                subject = this.subject!!,
                content = this.content!!,
                modifiedBy = this.modifiedBy,
                modifiedAt = OffsetDateTime.of(this.modifiedAt, ZoneOffset.UTC),
                createdBy = this.createdBy,
                createdAt = OffsetDateTime.of(this.createdAt, ZoneOffset.UTC),
            )

        internal fun NotificationTemplate.toRecordForCreation() =
            NotificationTemplateRecord().also {
                it.channel = toRecord(this.channel)
                it.type = toRecord(this.type)
                it.language = toRecord(this.language)
                it.subject = this.subject
                it.content = this.content
                it.modifiedBy = this.modifiedBy
                it.modifiedAt = this.modifiedAt.toLocalDateTime()
                it.createdBy = this.createdBy
                it.createdAt = this.createdAt.toLocalDateTime()
            }

        private fun toDomain(from: RecordNotificationChannel) =
            when (from) {
                RecordNotificationChannel.EMAIL -> NotificationChannel.EMAIL
            }

        private fun toDomain(from: RecordNotificationType) =
            when (from) {
                RecordNotificationType.WELCOME -> NotificationType.WELCOME
                RecordNotificationType.PASSWORD_RESET -> NotificationType.PASSWORD_RESET
            }

        private fun toDomain(from: RecordNotificationLanguage) =
            when (from) {
                RecordNotificationLanguage.EN -> NotificationLanguage.EN
            }

        private fun toRecord(from: NotificationChannel) =
            when (from) {
                NotificationChannel.EMAIL -> RecordNotificationChannel.EMAIL
            }

        private fun toRecord(from: NotificationType) =
            when (from) {
                NotificationType.WELCOME -> RecordNotificationType.WELCOME
                NotificationType.PASSWORD_RESET -> RecordNotificationType.PASSWORD_RESET
            }

        private fun toRecord(from: NotificationLanguage) =
            when (from) {
                NotificationLanguage.EN -> RecordNotificationLanguage.EN
            }
    }
}
