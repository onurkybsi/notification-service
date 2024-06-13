package org.kybprototyping.notificationservice.domain.usecase.notificationtemplate.retrieval

import org.kybprototyping.notificationservice.domain.model.NotificationTemplate
import org.kybprototyping.notificationservice.domain.port.NotificationTemplateRepositoryPort
import org.kybprototyping.notificationservice.domain.common.interfaces.InputOutputUseCaseHandler
import org.kybprototyping.notificationservice.domain.common.exception.nonExistentData

internal class NotificationTemplateRetrievalUseCaseHandler(
    private val notificationTemplateRepositoryAdapter: NotificationTemplateRepositoryPort
) : InputOutputUseCaseHandler<Int, NotificationTemplate> {

    override suspend fun handle(input: Int): NotificationTemplate =
        notificationTemplateRepositoryAdapter.getById(input) ?: throw nonExistentData("No notification template exists with given ID $input!")

}