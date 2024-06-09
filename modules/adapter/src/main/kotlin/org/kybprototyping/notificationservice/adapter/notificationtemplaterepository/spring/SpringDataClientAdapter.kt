package org.kybprototyping.notificationservice.adapter.notificationtemplaterepository.spring

import kotlinx.coroutines.reactive.awaitSingle
import org.kybprototyping.notificationservice.domain.model.*
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitSingleOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Component
internal class SpringDataClientAdapter(
    private val databaseClient: DatabaseClient,
    private val mapper: NotificationTemplateMapper
) : NotificationTemplateRepositoryPort {

    @Transactional
    override suspend fun create(request: NotificationTemplateCreationRequest): Int {
        val values = mapOf(
            "modificationDate" to OffsetDateTime.now(),
            "creationDate" to OffsetDateTime.now(),
            "channel" to mapper.toEntity(request.channel),
            "type" to mapper.toEntity(request.type),
            "language" to mapper.toEntity(request.language),
            "content" to request.content)
        return databaseClient.sql("""
            INSERT INTO public.notification_template (modification_date, creation_date, channel, type, language, content)
            VALUES(:modificationDate, :creationDate, :channel, :type, :language, :content)
            RETURNING id
            """.trimIndent()
        ).bindValues(values)
            .map { row -> row.get("id") as Int }
            .awaitSingleOrNull()!!
    }

    @Transactional(readOnly = true)
    override suspend fun getListBy(
        channel: NotificationChannel?,
        type: NotificationType?,
        language: NotificationLanguage?
    ): List<NotificationTemplate> {
        val whereClause = prepareWhereClause(channel, type, language)
        return if (whereClause == null) {
            databaseClient.sql("SELECT * FROM public.notification_template")
                .map { row -> mapper.toDto(row) }
                .all()
                .collectList()
                .awaitSingle()
        } else {
            databaseClient.sql( "SELECT * FROM public.notification_template WHERE ${whereClause.first}")
                .bindValues(whereClause.second)
                .map { row -> mapper.toDto(row) }
                .all()
                .collectList()
                .awaitSingle()
        }
    }

    @Transactional(readOnly = true)
    override suspend fun getOneBy(
        channel: NotificationChannel,
        type: NotificationType,
        language: NotificationLanguage
    ): NotificationTemplate? =
        databaseClient.sql("""
            SELECT * FROM public.notification_template
            WHERE channel = :channel AND type = :type AND language = :language
            """
        )
            .bind("channel", mapper.toEntity(channel))
            .bind("type", mapper.toEntity(type))
            .bind("language", mapper.toEntity(language))
            .map { row -> mapper.toDto(row) }
            .awaitSingleOrNull()

    @Transactional(readOnly = true)
    override suspend fun getById(id: Int): NotificationTemplate? =
        databaseClient.sql("SELECT * FROM public.notification_template WHERE id = :id")
            .bind("id", id)
            .map { row -> mapper.toDto(row) }
            .awaitSingleOrNull()

    @Transactional
    override suspend fun updateContent(id: Int, content: String): NotificationTemplate? =
        databaseClient.sql("""
            UPDATE public.notification_template
            SET content = :content, modification_date = :modificationDate
            WHERE id = :id
            RETURNING *
            """.trimIndent()
        )
            .bind("id", id)
            .bind("content", content)
            .bind("modificationDate", OffsetDateTime.now())
            .map { row -> mapper.toDto(row) }
            .awaitSingleOrNull()

    @Transactional
    override suspend fun delete(id: Int): NotificationTemplate? =
        databaseClient.sql("""
            DELETE FROM public.notification_template
            WHERE id = :id
            RETURNING *
            """.trimIndent()
        )
            .bind("id", id)
            .map { row -> mapper.toDto(row) }
            .awaitSingleOrNull()

    private fun prepareWhereClause(
        channel: NotificationChannel?,
        type: NotificationType?,
        language: NotificationLanguage?): Pair<String, Map<String, Any>>? {
        val whereClause = StringBuilder()
        val values = mutableMapOf<String, Any>()
        if (channel != null) {
            whereClause.append("(:channel IS NULL OR channel = :channel)")
            values["channel"] = mapper.toEntity(channel)
        }
        if (type != null) {
            if (whereClause.isNotEmpty())
                whereClause.append(" AND ")
            whereClause.append("(:type IS NULL OR type = :type)")
            values["type"] = mapper.toEntity(type)
        }
        if (language != null) {
            if (whereClause.isNotEmpty())
                whereClause.append(" AND ")
            whereClause.append("(:language IS NULL OR language = :language)")
            values["language"] = mapper.toEntity(language)
        }
        return if(whereClause.isEmpty()) {
            null
        } else {
            whereClause.toString() to values
        }
    }

}