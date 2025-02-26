package org.kybprototyping.notificationservice.domain.port

import arrow.core.Either
import org.kybprototying.notificationservice.common.Failure

interface TransactionalExecutor {
    /**
     * Executes the given [block] in a transactional way.
     *
     * It begins a transaction for *all the ports that supports transactional executions* such as repository ports.
     * In case of no left value returned from given [block], the transactions began are committed.
     * In case of a left value returned or an exception occurrence, the transactions are rolled back
     * and the exception is propagated if occurred.
     *
     * @param block function block to be executed transactional
     * @return execution result of the given [block]
     */
    suspend fun <T> execute(block: suspend () -> Either<Failure, T>): Either<Failure, T>
}
