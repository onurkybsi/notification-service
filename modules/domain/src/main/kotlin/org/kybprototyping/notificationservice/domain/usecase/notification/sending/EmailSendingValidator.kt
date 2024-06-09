package org.kybprototyping.notificationservice.domain.usecase.notification.sending

import org.kybprototyping.notificationservice.common.ValidationResult
import org.kybprototyping.notificationservice.common.Validator
import java.util.regex.Pattern

private val REGEX_EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$")

internal class EmailSendingValidator : Validator<EmailSendingInput> {

    override fun validate(validated: EmailSendingInput): ValidationResult {
        val result = ValidationResult()

        if (!REGEX_EMAIL.matcher(validated.to).matches()) {
            result.addFailure("to","must be an email address")
        }

        return result
    }

}