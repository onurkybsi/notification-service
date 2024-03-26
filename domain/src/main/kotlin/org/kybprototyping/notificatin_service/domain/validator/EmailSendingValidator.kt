package org.kybprototyping.notificatin_service.domain.validator

import org.kybprototyping.notificatin_service.domain.dto.EmailSendingInput
import org.kybprototyping.notificatin_service.domain.interfaces.Validator
import org.kybprototyping.notificatin_service.domain.interfaces.Validator.ValidationResult
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