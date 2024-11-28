package org.kybprototyping.notificationservice.adapter.repository.notificationtemplate

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import org.jooq.DSLContext
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.tables.records.NotificationTemplateRecord
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.tables.references.NOTIFICATION_TEMPLATE
import org.kybprototyping.notificationservice.domain.common.UnexpectedFailure
import org.kybprototyping.notificationservice.domain.model.NotificationChannel
import org.kybprototyping.notificationservice.domain.model.NotificationLanguage
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate
import org.kybprototyping.notificationservice.domain.model.NotificationType
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

// TODO: Make this Spring transactional management compatible.
@Component
@ConditionalOnProperty(
    value = ["ports.notification-template-repository.impl"],
    havingValue = "jooq",
)
internal class JooqImpl(private val dslContext: DSLContext) : NotificationTemplateRepositoryPort {
    override suspend fun findById(id: Int) =
        try {
            dslContext.selectFrom(NOTIFICATION_TEMPLATE)
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
        var query = dslContext.selectFrom(NOTIFICATION_TEMPLATE).where()
        channel?.let { query = query.and(NOTIFICATION_TEMPLATE.CHANNEL.eq(channel.name)) }
        type?.let { query = query.and(NOTIFICATION_TEMPLATE.TYPE.eq(type.name)) }
        language?.let { query = query.and(NOTIFICATION_TEMPLATE.LANGUAGE.eq(language.name)) }

        Flux.from(query)
            .map { it.toDomain() }
            .collectList()
            .awaitSingle()
            .right()
    } catch (e: Exception) {
        UnexpectedFailure(cause = e).left()
    }

    override suspend fun create(
        channel: NotificationChannel,
        type: NotificationType,
        language: NotificationLanguage,
        subject: String,
        content: String,
    ) = try {
        dslContext
            .insertInto(NOTIFICATION_TEMPLATE)
            .set(NOTIFICATION_TEMPLATE.CHANNEL, channel.name)
            .set(NOTIFICATION_TEMPLATE.TYPE, type.name)
            .set(NOTIFICATION_TEMPLATE.LANGUAGE, language.name)
            .set(NOTIFICATION_TEMPLATE.SUBJECT, subject)
            .set(NOTIFICATION_TEMPLATE.CONTENT, content)
            .set(NOTIFICATION_TEMPLATE.MODIFIED_AT, LocalDateTime.now()) // TODO: TimeUtils!
            .set(NOTIFICATION_TEMPLATE.CREATED_AT, LocalDateTime.now()) // TODO: TimeUtils!
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
            dslContext
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
                dslContext
                    .update(NOTIFICATION_TEMPLATE)
                    .set(NOTIFICATION_TEMPLATE.MODIFIED_AT, LocalDateTime.now()) // TODO: TimeUtils!
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
                channel = NotificationChannel.valueOf(this.channel!!),
                type = NotificationType.valueOf(this.type!!),
                language = NotificationLanguage.valueOf(this.language!!),
                subject = this.subject!!,
                content = this.content!!,
                modifiedBy = this.modifiedBy,
                modifiedAt = OffsetDateTime.of(this.modifiedAt, ZoneOffset.UTC),
                createdBy = this.createdBy,
                createdAt = OffsetDateTime.of(this.createdAt, ZoneOffset.UTC),
            )

        internal fun NotificationTemplate.toRecordForCreation() =
            NotificationTemplateRecord().also {
                it.channel = this.channel.name
                it.type = this.type.name
                it.language = this.language.name
                it.subject = this.subject
                it.content = this.content
                it.modifiedBy = this.modifiedBy
                it.modifiedAt = this.modifiedAt.toLocalDateTime() // TODO: TimeUtils!
                it.createdBy = this.createdBy
                it.createdAt = this.createdAt.toLocalDateTime() // TODO: TimeUtils!
            }
    }
}
