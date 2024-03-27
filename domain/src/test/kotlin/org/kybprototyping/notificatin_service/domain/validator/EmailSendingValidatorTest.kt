package org.kybprototyping.notificatin_service.domain.validator

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.kybprototyping.notificatin_service.domain.dto.EmailLanguage
import org.kybprototyping.notificatin_service.domain.dto.EmailSendingInput
import org.kybprototyping.notificatin_service.domain.dto.EmailType
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmailSendingValidatorTest {

    private val underTest: EmailSendingValidator = EmailSendingValidator()

    @ParameterizedTest
    @ValueSource(strings = ["", " ", "something@"])
    fun validate_Should_Return_Invalid_When_Given_To_Is_Not_Email(to: String) {
        // given
        val input = EmailSendingInput(EmailType.WELCOME, EmailLanguage.EN, to)

        // when
        val actualResult = underTest.validate(input)

        // then
        assertFalse { actualResult.isValid() }
        assertEquals(mapOf(Pair("to", listOf("must be an email address"))), actualResult.getFailures())
    }

    @Test
    fun validate_Should_Return_Valid_When_All_Given_Values_Are_Valid() {
        // given
        val input = EmailSendingInput(EmailType.WELCOME, EmailLanguage.EN, "something@something")

        // when
        val actualResult = underTest.validate(input)

        // then
        assertTrue { actualResult.isValid() }
        assertTrue { actualResult.getFailures().isEmpty() }
    }

}