package org.kybprototyping.notificationservice.adapter.repository.servicetask

import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.JSON
import org.kybprototying.notificationservice.common.DataConflictFailure
import org.kybprototying.notificationservice.common.TimeUtils
import org.kybprototying.notificationservice.common.UnexpectedFailure
import org.kybprototying.notificationservice.common.runExceptionCatching
import org.kybprototyping.notificationservice.adapter.repository.common.TransactionAwareDSLContextProxy
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.Tables.SERVICE_TASK
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.tables.records.ServiceTaskRecord
import org.kybprototyping.notificationservice.domain.model.ServiceTask
import org.kybprototyping.notificationservice.domain.model.ServiceTaskPriority
import org.kybprototyping.notificationservice.domain.model.ServiceTaskStatus
import org.kybprototyping.notificationservice.domain.model.ServiceTaskType
import org.kybprototyping.notificationservice.domain.port.ServiceTaskRepositoryPort
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.enums.ServiceTaskStatus as RecordServiceTaskStatus
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.enums.ServiceTaskType as RecordServiceTaskType

@Component
internal class JooqImpl(
    private val transactionAwareDSLContextProxy: TransactionAwareDSLContextProxy,
    private val timeUtils: TimeUtils,
) : ServiceTaskRepositoryPort {
    private val objectMapper = jacksonObjectMapper() // TODO: Use a common one!

    override suspend fun insert(task: ServiceTask) =
        runExceptionCatching {
            val numOfAffectedRows =
                transactionAwareDSLContextProxy.dslContext()
                    .insertInto(SERVICE_TASK)
                    .set(toRecord(task))
                    .onConflictDoNothing()
                    .awaitSingle()

            if (numOfAffectedRows == 1) {
                Unit.right()
            } else {
                transactionAwareDSLContextProxy.dslContext()
                    .selectOne()
                    .from(SERVICE_TASK)
                    .where(SERVICE_TASK.EXTERNAL_ID.eq(task.externalId))
                    .limit(1)
                    .awaitFirstOrNull()
                    ?.let { DataConflictFailure("External ID already exists: ${task.externalId}").left() }
                    ?: UnexpectedFailure("Primary key conflict: ${task.id}").left()
            }
        }

    override suspend fun updateBy(status: ServiceTaskStatus, statusToSet: ServiceTaskStatus) =
        runExceptionCatching {
            transactionAwareDSLContextProxy.dslContext()
                .update(SERVICE_TASK)
                .set(SERVICE_TASK.STATUS, toRecord(statusToSet))
                .set(SERVICE_TASK.MODIFIED_AT, timeUtils.nowAsLocalDateTime())
                .where(SERVICE_TASK.STATUS.eq(toRecord(status)))
                .returning()
                .let { publisher -> Flux.from(publisher) }
                .map { updatedRecord -> toDomain(updatedRecord) }
                .collectList()
                .awaitSingle()
                .right()
        }

    internal fun toRecord(from: ServiceTask) =
        ServiceTaskRecord().also {
            it.id = from.id
            it.type = toRecord(from.type)
            it.status = toRecord(from.status)
            it.externalId = from.externalId
            it.priority = from.priority.value().toShort()
            it.executionCount = from.executionCount.toShort()
            it.executionStartedAt = from.executionStartedAt?.toLocalDateTime()
            it.executionScheduledAt = from.executionScheduledAt?.toLocalDateTime()
            it.input = from.input?.let { i -> JSON.valueOf(objectMapper.writeValueAsString(i)) } // TODO: These should be JSONB, there is an issue with jOOQ generator!
            it.output = from.output?.let { o -> JSON.valueOf(objectMapper.writeValueAsString(o)) }
            it.message = from.message
            it.modifiedAt = from.modifiedAt.toLocalDateTime()
            it.createdAt = from.createdAt.toLocalDateTime()
        }

    internal fun toDomain(from: ServiceTaskRecord) =
        ServiceTask(
            id = from.id,
            type = toDomain(from.type),
            status = toDomain(from.status),
            externalId = from.externalId,
            priority = ServiceTaskPriority.valueOf(from.priority.toInt())!!,
            executionCount = from.executionCount.toInt(),
            executionStartedAt = from.executionStartedAt?.let { OffsetDateTime.from(it) },
            executionScheduledAt = from.executionScheduledAt?.let { OffsetDateTime.from(it) },
            input = from.input?.let { objectMapper.readTree(it.data()) },
            output = from.output?.let { objectMapper.readTree(it.data()) },
            message = from.message,
            modifiedAt = timeUtils.toOffsetDateTime(from.modifiedAt),
            createdAt = timeUtils.toOffsetDateTime(from.createdAt),
        )

    internal companion object {
        internal fun toRecord(from: ServiceTaskType) =
            when (from) {
                ServiceTaskType.SEND_EMAIL -> RecordServiceTaskType.SEND_EMAIL
            }

        internal fun toRecord(from: ServiceTaskStatus) =
            when (from) {
                ServiceTaskStatus.PENDING -> RecordServiceTaskStatus.PENDING
                ServiceTaskStatus.IN_PROGRESS -> RecordServiceTaskStatus.IN_PROGRESS
                ServiceTaskStatus.ERROR -> RecordServiceTaskStatus.ERROR
                ServiceTaskStatus.COMPLETED -> RecordServiceTaskStatus.COMPLETED
                ServiceTaskStatus.FAILED -> RecordServiceTaskStatus.FAILED
                ServiceTaskStatus.PUBLISHED -> RecordServiceTaskStatus.PUBLISHED
            }

        private fun toDomain(from: RecordServiceTaskType) =
            when (from) {
                RecordServiceTaskType.SEND_EMAIL -> ServiceTaskType.SEND_EMAIL
            }

        private fun toDomain(from: RecordServiceTaskStatus) =
            when (from) {
                RecordServiceTaskStatus.PENDING -> ServiceTaskStatus.PENDING
                RecordServiceTaskStatus.IN_PROGRESS -> ServiceTaskStatus.IN_PROGRESS
                RecordServiceTaskStatus.ERROR -> ServiceTaskStatus.ERROR
                RecordServiceTaskStatus.COMPLETED -> ServiceTaskStatus.COMPLETED
                RecordServiceTaskStatus.FAILED -> ServiceTaskStatus.FAILED
                RecordServiceTaskStatus.PUBLISHED -> ServiceTaskStatus.PUBLISHED
            }
    }
}
