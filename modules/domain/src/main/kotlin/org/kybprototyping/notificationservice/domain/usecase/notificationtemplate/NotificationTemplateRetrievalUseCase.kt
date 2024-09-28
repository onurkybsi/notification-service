package org.kybprototyping.notificationservice.domain.usecase.notificationtemplate

import arrow.core.Either
import org.kybprototyping.notificationservice.domain.common.Failure
import org.kybprototyping.notificationservice.domain.common.UseCaseHandler
import org.kybprototyping.notificationservice.domain.model.NotificationTemplate

internal class NotificationTemplateRetrievalUseCase : UseCaseHandler<Int, NotificationTemplate> {
    override suspend fun handle(input: Int): Either<Failure, NotificationTemplate> =
        TODO("Will be implemented!")
}