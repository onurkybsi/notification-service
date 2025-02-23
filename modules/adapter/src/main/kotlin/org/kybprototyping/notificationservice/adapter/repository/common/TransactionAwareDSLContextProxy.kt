package org.kybprototyping.notificationservice.adapter.repository.common

import io.r2dbc.spi.ConnectionFactory
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.kybprototyping.notificationservice.adapter.repository.common.TransactionalExecutorSpringTransactionManagerImpl.TransactionContext
import kotlin.coroutines.coroutineContext

internal class TransactionAwareDSLContextProxy(connectionFactory: ConnectionFactory) {
    private val transactionUnawareDSLContext = DSL.using(connectionFactory, SQLDialect.POSTGRES)

    /**
     * Returns the transactional [DSLContext] if there is a transaction began.
     *
     * If there is no transaction already began, it returns the common [DSLContext].
     *
     * @return transaction aware [DSLContext]
     */
    internal suspend fun dslContext() = coroutineContext[TransactionContext]?.dslContext ?: transactionUnawareDSLContext
}
