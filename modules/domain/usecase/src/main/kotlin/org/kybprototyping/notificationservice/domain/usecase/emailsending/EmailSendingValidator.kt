package org.kybprototyping.notificationservice.domain.usecase.emailsending

import org.kybprototyping.notificationservice.domain.model.EmailSendingInput
import org.kybprototyping.notificationservice.domain.usecase.Validator
import java.util.regex.Pattern

private val REGEX_EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$")

internal class EmailSendingValidator : Validator<EmailSendingInput> {

    override fun validate(validated: EmailSendingInput): Validator.ValidationResult {
        val result = Validator.ValidationResult()

        if (!REGEX_EMAIL.matcher(validated.to).matches()) {
            result.addFailure("to","must be an email address")
        }

        return result
    }

}