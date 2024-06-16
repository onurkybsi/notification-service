package org.kybprototyping.notificationservice.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

internal class ValidationResultTest {

    @Test
    fun `should add given failure message with field name to failures`() {
        // given
        val result = ValidationResult()

        // when
        result.addFailure("fieldX", "something wrong")

        // then
        assertThat(result.getFailures()).isEqualTo(mapOf(Pair("fieldX", listOf("something wrong"))))
    }

    @Test
    fun `should return valid when there is no any field failure`() {
        // given
        val result = ValidationResult()

        // when
        val actualResult = result.isNotValid()

        // then
        assertFalse { actualResult }
    }

    @Test
    fun `should return invalid when there are some field failures`() {
        // given
        val result = ValidationResult()
        result.addFailure("fieldX", "something wrong")

        // when
        val actualResult = result.isNotValid()

        // then
        assertTrue { actualResult }
    }

    @Test
    @Suppress("unchecked_cast")
    fun `should return deep copy of field failures`() {
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
        assertThat(actualResult.values).allMatch { v -> failures.values.stream().allMatch { v !== it } }
        assertThat(actualResult.size).isEqualTo(2)
        assertThat(actualResult["fieldX"]).hasSize(2).contains("cannot be null", "must be email formatted")
        assertThat(actualResult["fieldY"]).hasSize(1).contains("cannot be null")
    }

    @Test
    fun `should build ValidationResult from given failure pairs`() {
        // given
        val fieldXFailures = "fieldX" to arrayOf("cannot be null")
        val fieldYFailures = "fieldY" to arrayOf("cannot be null")

        // when
        val actualResult = ValidationResult.from(fieldXFailures, fieldYFailures)

        // then
        assertTrue(actualResult.isNotValid())
        assertThat(actualResult.getFailures().size).isEqualTo(2)
        assertThat(actualResult.getFailures()["fieldX"]).hasSize(1).contains("cannot be null")
        assertThat(actualResult.getFailures()["fieldY"]).hasSize(1).contains("cannot be null")
    }

}