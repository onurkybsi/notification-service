package org.kybprototyping.notificatin_service.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.kybprototyping.notificatin_service.domain.dto.EmailSendingInput
import org.kybprototyping.notificatin_service.domain.exception.UseCaseException
import org.kybprototyping.notificatin_service.domain.interfaces.Validator
import org.kybprototyping.notificatin_service.domain.interfaces.Validator.ValidationResult
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
internal class EmailSendingUseCaseHandlerTest {

    @Mock
    lateinit var validator: Validator<EmailSendingInput>

    @InjectMocks
    lateinit var underTest: EmailSendingUseCaseHandler

    @Test
    fun handle_Should_Throw_DataInvalidityException_When_Given_Input_Is_Not_Valid() {
        // given
        val input = EmailSendingInput("")
        val validationResult = ValidationResult()
        validationResult.addFailure("to", "must be an email address")
        Mockito.`when`(validator.validate(input)).thenReturn(validationResult)

        // when
        val thrownException: UseCaseException = assertThrows { underTest.handle(input) } as UseCaseException

        // then
        assertEquals("Input is not valid!", thrownException.message)
        assertTrue { thrownException.dueToDataInvalidity }
        assertEquals(mapOf(Pair("to", listOf("must be an email address"))), thrownException.failures)
    }

}