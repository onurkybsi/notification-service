package org.kybprototyping.notificationservice.domain.common

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Provides particular coroutine dispatchers for particular executions.
 */
interface CoroutineDispatcherProvider {
    val serviceTaskExecutorDispatcher: CoroutineDispatcher
}
