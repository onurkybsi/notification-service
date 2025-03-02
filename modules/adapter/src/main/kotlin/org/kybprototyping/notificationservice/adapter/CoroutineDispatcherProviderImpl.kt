package org.kybprototyping.notificationservice.adapter

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import org.kybprototyping.notificationservice.domain.common.CoroutineDispatcherProvider
import org.springframework.stereotype.Component
import java.util.concurrent.Executors

@Component
internal class CoroutineDispatcherProviderImpl : CoroutineDispatcherProvider {
    // TODO: Make it monitor able!
    override val serviceTaskExecutorDispatcher: CoroutineDispatcher = Executors.newCachedThreadPool().asCoroutineDispatcher()
}