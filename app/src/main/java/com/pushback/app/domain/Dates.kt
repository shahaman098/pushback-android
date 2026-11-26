package com.pushback.app.domain

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object Dates {
    private val iso: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val zone: ZoneId get() = ZoneId.systemDefault()

    fun today(): LocalDate = LocalDate.now(zone)
    fun todayIso(): String = today().format(iso)
    fun iso(date: LocalDate): String = date.format(iso)
    fun parse(isoDate: String): LocalDate = LocalDate.parse(isoDate, iso)

    fun startOfDay(date: LocalDate = today()): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()

    fun endOfDay(date: LocalDate = today()): Long =
        date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

    fun fromMillis(ms: Long): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), zone)

    fun hourOf(ms: Long): Int = fromMillis(ms).hour

    fun formatTime(hour: Int, minute: Int): String =
        LocalTime.of(hour, minute).format(DateTimeFormatter.ofPattern("HH:mm"))
}
