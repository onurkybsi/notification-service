package org.kybprototying.notificationservice.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

internal class TimeUtilsTest {
    private val underTest: TimeUtils =
        TimeUtils(Clock.fixed(Instant.ofEpochSecond(1735689600), ZoneId.of("UTC")))

    @Test
    fun `should return current-date as LocalDateTime from the configured clock`() {
        // given

        // when
        val actual = underTest.nowAsLocalDateTime()

        // then
        assertThat(actual).isEqualTo(LocalDateTime.parse("2025-01-01T00:00:00"))
    }

    @Test
    fun `should convert given LocalDateTime to OffsetDateTime by the configured clock`() {
        // given
        val from = LocalDateTime.parse("2025-01-01T00:00:00")

        // when
        val actual = underTest.toOffsetDateTime(from)

        // then
        assertThat(actual).isEqualTo(OffsetDateTime.parse("2025-01-01T00:00Z"))
    }

}