package com.pushback.app.domain

import android.content.Context
import org.json.JSONArray

class MentalityEngine(context: Context) {
    val cards: List<MentalityCard> = context.assets.open("mentality.json").bufferedReader().use { reader ->
        val arr = JSONArray(reader.readText())
        buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(
                    MentalityCard(
                        id = o.getString("id"),
                        thinker = o.getString("thinker"),
                        line = o.getString("line"),
                        failureMode = FailureMode.valueOf(o.getString("failureMode").uppercase()),
                    ),
                )
            }
        }
    }

    fun forMode(mode: FailureMode): MentalityCard {
        val pool = cards.filter { it.failureMode == mode }.ifEmpty { cards }
        val day = Dates.today().toEpochDay().toInt()
        return pool[Math.floorMod(day, pool.size)]
    }

    fun byId(id: String): MentalityCard? = cards.find { it.id == id }

    fun inferMode(
        sentToday: Int,
        quota: Int,
        eveningDone: Boolean,
        wasteInterrupts: Int,
        postponed: Boolean,
    ): FailureMode = when {
        !eveningDone && Dates.todayIso().let { true } && LocalNow.hour() >= 21 && sentToday >= quota ->
            FailureMode.NO_REVIEW
        sentToday == 0 && postponed -> FailureMode.AVOIDANCE
        sentToday == 0 -> FailureMode.NO_SEND
        wasteInterrupts >= 2 -> FailureMode.SCROLLING
        sentToday < quota -> FailureMode.FAKE_WORK
        else -> FailureMode.NO_REVIEW
    }
}

private object LocalNow {
    fun hour(): Int = java.time.LocalTime.now().hour
}
