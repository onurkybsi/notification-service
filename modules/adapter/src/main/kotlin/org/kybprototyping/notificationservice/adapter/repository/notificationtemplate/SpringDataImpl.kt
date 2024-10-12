package org.kybprototyping.notificationservice.adapter.repository.notificationtemplate

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.NotificationTemplate.Companion.toDomain
import org.kybprototyping.notificationservice.domain.common.UnexpectedFailure
import org.kybprototyping.notificationservice.domain.model.NotificationChannel as DomainNotificationChannel
import org.kybprototyping.notificationservice.domain.model.NotificationLanguage as DomainNotificationLanguage
import org.kybprototyping.notificationservice.domain.model.NotificationType as DomainNotificationType
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort.DeletionFailure
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort.UpdateFailure
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.dao.DuplicateKeyException
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate as DomainNotificationTemplate
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Query.query
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@ConditionalOnProperty(
    value = ["ports.notification-template-repository.impl"],
    havingValue = "spring-data"
)
internal class SpringDataImpl(private val entityTemplate: R2dbcEntityTemplate) : NotificationTemplateRepositoryPort {
    override suspend fun findById(id: Int): Either<UnexpectedFailure, DomainNotificationTemplate?> =
        try {
            entityTemplate.selectOne(query(where("id").`is`(id)), NotificationTemplate::class.java)
                .awaitSingleOrNull()
                ?.toDomain()
                .right()
        } catch (e: Exception) {
            UnexpectedFailure(isTemporary = true, cause = e).left()
        }

    override suspend fun findBy(
        channel: DomainNotificationChannel?,
        type: DomainNotificationType?,
        language: DomainNotificationLanguage?
    ): Either<UnexpectedFailure, List<DomainNotificationTemplate>> =
        try {
            val criteria = mutableListOf<Criteria>()
            channel?.let { criteria.add(where("channel").`is`(it)) }
            type?.let { criteria.add(where("type").`is`(it)) }
            language?.let { criteria.add(where("language").`is`(it)) }

            val query = if (criteria.isNotEmpty()) { query(criteria.reduce { acc, c -> acc.and(c) }) } else Query.empty()

            entityTemplate
                .select(query, NotificationTemplate::class.java)
                .map { it.toDomain() }
                .collectList()
                .awaitSingle()
                .right()
        } catch (e: Exception) {
            UnexpectedFailure(isTemporary = true, cause = e).left()
        }

    override suspend fun create(
        channel: DomainNotificationChannel,
        type: DomainNotificationType,
        language: DomainNotificationLanguage,
        subject: String,
        content: String
    ): Either<UnexpectedFailure, Int?> =
        try {
        // TODO: Add modified_at, created_at!
        entityTemplate.databaseClient.sql(
            """
                INSERT INTO notification_template (channel, type, language, subject, content, modified_at, created_at)
                VALUES (:channel, :type, :language, :subject, :content, :modified_at, :created_at)
                RETURNING id
            """
        )
            .bind("channel", channel.name)
            .bind("type", type.name)
            .bind("language", language.name)
            .bind("subject", subject)
            .bind("content", content)
            .bind("modified_at", LocalDateTime.now()) // TODO: TimeUtils!
            .bind("created_at", LocalDateTime.now())
            .map { row -> row.get("id", Integer::class.java)!!.toInt() }
            .one()
            .awaitSingleOrNull()!!
            .right()
        } catch (e: DuplicateKeyException) {
            null.right()
        } catch (e: Exception) {
            UnexpectedFailure(isTemporary = true, cause = e).left()
        }

    override suspend fun delete(id: Int): Either<DeletionFailure, Unit> =
        try {
            entityTemplate
                .delete(NotificationTemplate::class.java)
                .matching(query(where("id").`is`(id)))
                .all()
                .awaitSingle()
                .let { rowsDeleted ->
                    if (rowsDeleted == 0L) {
                        DeletionFailure.DataNotFoundFailure.left()
                    } else {
                        Unit.right()
                    }
                }
        } catch (e: Exception) {
            DeletionFailure.UnexpectedFailure(e).left()
        }

    override suspend fun update(
        id: Int,
        subjectToSet: String?,
        contentToSet: String?
    ): Either<UpdateFailure, Unit> {
        try {
            if (subjectToSet == null && contentToSet == null)
                return Unit.right()

            val columnsToUpdate = mutableListOf("modified_at = :modifiedAt")
            val valuesToBind = mutableMapOf<String, Any>("id" to id, "modifiedAt" to LocalDateTime.now()) // TODO: TimeUtils!
            subjectToSet?.let { columnsToUpdate.add("subject = :subject"); valuesToBind["subject"] = subjectToSet }
            contentToSet?.let { columnsToUpdate.add("content = :content"); valuesToBind["content"] = contentToSet }

            entityTemplate.databaseClient.sql(
                """
                    UPDATE notification_template 
                    SET ${columnsToUpdate.joinToString(", ")}
                    WHERE id = :id
                """
            )
                .bindValues(valuesToBind)
                .fetch()
                .rowsUpdated()
                .awaitSingle()
                .let { rowsUpdated ->
                    return if (rowsUpdated > 0) {
                        Unit.right()
                    } else {
                        UpdateFailure.DataNotFoundFailure.left()
                    }
                }
        } catch (e: Exception) {
            return UpdateFailure.UnexpectedFailure(e).left()
        }
    }
}