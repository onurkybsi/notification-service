package org.kybprototyping.notificationservice.adapter.common

import org.springframework.core.convert.converter.Converter
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

object CommonR2dbcConverters {

    internal object OffsetDateTimeConverter : Converter<LocalDateTime, OffsetDateTime> {
        override fun convert(source: LocalDateTime): OffsetDateTime = OffsetDateTime.of(source, ZoneOffset.UTC)
    }

}