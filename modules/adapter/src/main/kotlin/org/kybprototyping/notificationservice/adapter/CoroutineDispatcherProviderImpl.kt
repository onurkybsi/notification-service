package org.kybprototyping.notificationservice.adapter

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import org.kybprototyping.notificationservice.domain.common.CoroutineDispatcherProvider
import org.springframework.stereotype.Component
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

@Component
internal class CoroutineDispatcherProviderImpl : CoroutineDispatcherProvider {
    // TODO: Make it monitor able!
    override val serviceTaskExecutorDispatcher: CoroutineDispatcher = buildCoroutineDispatcher("service-task-executor")

    internal companion object {
        private const val DEFAULT_KEEP_ALIVE_TIME_SEC = 60L

        internal fun buildCoroutineDispatcher(name: String, corePoolSize: Int? = null, maxPoolSize: Int? = null) =
            ThreadPoolExecutor(
                corePoolSize ?: 0,
                maxPoolSize ?: Int.MAX_VALUE,
                DEFAULT_KEEP_ALIVE_TIME_SEC,
                TimeUnit.SECONDS,
                SynchronousQueue(), // Forces tasks to be executed immediately, no queueing!
                BasicThreadFactory.Builder().namingPattern("$name-thread-%d").build()
            )
                .asCoroutineDispatcher()
                .let { dispatcher ->
                    if (maxPoolSize != null) {
                        // TODO: Older version kotlinx.coroutines is used? This feature is not experimental!
                        dispatcher.limitedParallelism(maxPoolSize)  // Queueing happens here!
                    } else {
                        dispatcher
                    }
                }
    }
}
