package org.kybprototyping.notificationservice.domain.usecase.emailsending

import org.kybprototyping.notificationservice.common.Validator
import org.kybprototyping.notificationservice.domain.common.exception.dataInvalidity
import org.kybprototyping.notificationservice.domain.model.EmailSendingInput
import org.kybprototyping.notificationservice.domain.model.NotificationChannel
import org.kybprototyping.notificationservice.domain.port.notificationtemplaterepository.NotificationTemplateRepository
import org.kybprototyping.notificationservice.domain.usecase.InputOnlyUseCaseHandler

internal class EmailSendingUseCaseHandler(
    private val validator: Validator<EmailSendingInput>,
    private val notificationTemplateRepositoryAdapter: NotificationTemplateRepository
): InputOnlyUseCaseHandler<EmailSendingInput> {

    // @Transactional
    override suspend fun handle(input: EmailSendingInput) {
        // Step 1: Validate input
        val validationResult = validator.validate(input)
        if (validationResult.isNotValid()) {
            throw dataInvalidity("Input is not valid!", validationResult.getFailures())
        }
        // Step 2: Fetch template
        val template = notificationTemplateRepositoryAdapter.get(NotificationChannel.EMAIL, input.type, input.language)
        // Step 3: Prepare email content, (warning log if there are missing placeholder values)
        // Step 4: Store email to be sent
        // Step 5: Send email
    }

}