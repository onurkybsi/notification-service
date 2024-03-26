package org.kybprototyping.notificatin_service.domain

import org.kybprototyping.notificatin_service.domain.dto.EmailSendingInput
import org.kybprototyping.notificatin_service.domain.exception.dataInvalidity
import org.kybprototyping.notificatin_service.domain.interfaces.InputOnlyUseCaseHandler
import org.kybprototyping.notificatin_service.domain.interfaces.Validator

internal class EmailSendingUseCaseHandler(private val validator: Validator<EmailSendingInput>) : InputOnlyUseCaseHandler<EmailSendingInput> {

    // @Transactional
    override fun handle(input: EmailSendingInput) {
        // Step 1: Validate input
        val validationResult = validator.validate(input)
        if (!validationResult.isValid()) {
            throw dataInvalidity("Input is not valid!", validationResult.getFailures())
        }
        // Step 2: Fetch template
        // Step 3: Prepare email content, (warning log if there are missing placeholder values)
        // Step 4: Store email to be sent
        // Step 5: Send email
    }

}