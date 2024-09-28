package org.kybprototyping.notificationservice.adapter.repository.notificationtemplate

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.NotificationTemplate.Companion.toDomain
import org.kybprototyping.notificationservice.domain.common.UnexpectedFailure
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate as DomainNotificationTemplate
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.stereotype.Component

@Component
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
}