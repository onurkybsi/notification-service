package org.kybprototyping.notificatin_service.domain.interfaces

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import org.kybprototyping.notificatin_service.domain.interfaces.Validator.ValidationResult
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidationResultTest {

    @Test
    fun addFailure_Should_Add_GivenFailureMessage_For_FieldWithGivenName() {
        // given
        val result = ValidationResult()

        // when
        result.addFailure("fieldX", "something wrong")

        // then
        assertEquals("{fieldX=[something wrong]}", result.toString())
    }

    @Test
    fun  isValid_Should_Return_True_When_There_Is_NoFieldFailure() {
        // given
        val result = ValidationResult()

        // when
        val actualResult = result.isValid()

        // then
        assertTrue { actualResult }
    }

    @Test
    fun  isValid_Should_Return_False_When_There_Is_AnyFieldFailures() {
        // given
        val result = ValidationResult()
        result.addFailure("fieldX", "something wrong")

        // when
        val actualResult = result.isValid()

        // then
        assertFalse { actualResult }
    }

    @Test
    fun toString_Should_Return_FieldFailures_String() {
        // given
        val validationResult = ValidationResult()
        validationResult.addFailure("fieldX", "cannot be null")
        validationResult.addFailure("fieldX", "must be email formatted")
        validationResult.addFailure("fieldY", "cannot be null")

        // when
        val actualResult = validationResult.toString()

        // then
        assertEquals("{fieldY=[cannot be null], fieldX=[cannot be null, must be email formatted]}", actualResult)
    }

}