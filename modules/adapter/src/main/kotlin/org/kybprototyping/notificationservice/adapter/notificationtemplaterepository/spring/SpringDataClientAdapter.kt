package org.kybprototyping.notificationservice.adapter.notificationtemplaterepository.spring

import org.kybprototyping.notificationservice.domain.model.*
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitOne
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

private const val INSERTION_SCRIPT = """
    INSERT INTO public.notification_template (modification_date, creation_date, channel, type, language, content)
    VALUES(:modification_date, :creation_date, :channel, :type, :language, :content)
"""

@Component
internal open class SpringDataClientAdapter(
    private val databaseClient: DatabaseClient
): NotificationTemplateRepositoryPort {

    @Transactional
    override suspend fun create(request: NotificationTemplateCreationRequest): Long {
        val values: MutableMap<String, Any> = LinkedHashMap()
        values["modification_date"] = OffsetDateTime.now() // TODO: TimeUtils!
        values["creation_date"] = OffsetDateTime.now() // TODO: TimeUtils!
        values["channel"] = request.channel
        values["type"] = request.type
        values["language"] = request.language
        values["content"] = request.content
        return databaseClient.sql(INSERTION_SCRIPT).bindValues(values)
            .filter { s -> s.returnGeneratedValues("id") }
            .map { row -> row.get("id", String::class.java)?.toLong() ?: throw RuntimeException() /*TODO: Custom exception!*/ }
            .awaitOne()
    }

    override suspend fun getList(
        channel: NotificationChannel?,
        type: NotificationType?,
        language: NotificationLanguage?
    ): List<NotificationTemplate> {
        TODO("Not yet implemented")
    }

    override suspend fun get(
        channel: NotificationChannel,
        type: NotificationType,
        language: NotificationLanguage
    ): NotificationTemplate? {
        TODO("Not yet implemented")
    }

    override suspend fun getById(id: Long): NotificationTemplate? {
        TODO("Not yet implemented")
    }
}