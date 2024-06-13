package org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.deletion

import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort
import org.kybprototyping.notificationservice.domain.common.interfaces.InputOnlyUseCaseHandler
import org.kybprototyping.notificationservice.domain.common.exception.nonExistentData
import org.kybprototyping.notificationservice.domain.common.interfaces.Transactional

internal class NotificationTemplateDeletionUseCaseHandler(
    private val notificationTemplateRepositoryAdapter: NotificationTemplateRepositoryPort
): InputOnlyUseCaseHandler<Int> {

    @Transactional
    override suspend fun handle(input: Int) {
        notificationTemplateRepositoryAdapter.delete(input)
            ?: throw nonExistentData("No notification template exists with given ID ${input}!")
    }

}