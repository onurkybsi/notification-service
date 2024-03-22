package org.kybprototyping.notificatin_service.domain.interfaces

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import org.kybprototyping.notificatin_service.domain.interfaces.Validator.ValidationResult
import java.util.HashMap
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
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
    @Suppress("unchecked_cast")
    fun getFailures_Should_Return_DeepCopy_Of_FieldFailures() {
        // given
        val validationResult = ValidationResult()
        validationResult.addFailure("fieldX", "cannot be null")
        validationResult.addFailure("fieldX", "must be email formatted")
        validationResult.addFailure("fieldY", "cannot be null")

        val memberProperties = ValidationResult::class.memberProperties
        val failuresField = memberProperties.stream().filter { m -> m.name == "failures" }.findFirst().orElseThrow()
        failuresField.apply { isAccessible = true }
        val failures = failuresField.get(validationResult) as HashMap<String, ArrayList<String>>

        // when
        val actualResult = validationResult.getFailures()

        // then
        assertTrue { actualResult !== failures }
        assertTrue { actualResult.entries.stream().allMatch { ae -> failures.entries.stream().allMatch { fe -> fe !== ae } } }
        assertEquals(mapOf(Pair("fieldX", listOf("cannot be null", "must be email formatted")), Pair("fieldY", listOf("cannot be null"))), actualResult)
    }

}