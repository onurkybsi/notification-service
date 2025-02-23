package org.kybprototyping.notificationservice.adapter.repository.common

import arrow.core.Either
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.kybprototyping.notificationservice.domain.common.Failure
import org.kybprototyping.notificationservice.domain.port.TransactionalExecutor
import org.springframework.r2dbc.connection.ConnectionFactoryUtils
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import org.springframework.transaction.support.DefaultTransactionDefinition
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

internal class TransactionalExecutorSpringTransactionManagerImpl(
    private val connectionFactory: ConnectionFactory,
    transactionManager: ReactiveTransactionManager,
) : TransactionalExecutor {
    private val transactionalOperator =
        TransactionalOperator.create(
            transactionManager,
            DefaultTransactionDefinition().apply {
                propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
                isolationLevel = TransactionDefinition.ISOLATION_DEFAULT
            },
        )

    override suspend fun <T> execute(block: suspend () -> Either<Failure, T>): Either<Failure, T> =
        transactionalOperator.executeAndAwait { tx ->
            val transactionalConnection = ConnectionFactoryUtils.getConnection(connectionFactory).awaitSingle()
            val transactionalDSLContext = DSL.using(transactionalConnection, SQLDialect.POSTGRES)

            withContext(TransactionContext(transactionalDSLContext)) {
                block
                    .invoke()
                    .onLeft { tx.setRollbackOnly() }
            }
        }

    /**
     * Keeps the data that is attached to the current transaction.
     */
    internal data class TransactionContext(val dslContext: DSLContext) : AbstractCoroutineContextElement(Key) {
        companion object Key : CoroutineContext.Key<TransactionContext>
    }
}
