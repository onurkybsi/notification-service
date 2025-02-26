package org.kybprototying.notificationservice.common

import arrow.core.Either
import arrow.core.left

/**
 * Invokes the given [block] by catching the exception that might be thrown inside of [block].
 *
 * @param block function block to be invoked
 * @return invocation result of given [block], [UnexpectedFailure] if an exception is thrown
 */
inline fun <T> runExceptionCatching(block: () -> Either<Failure, T>): Either<Failure, T> =
    try {
        block()
    } catch (e: Exception) {
        UnexpectedFailure(cause = e).left()
    }
