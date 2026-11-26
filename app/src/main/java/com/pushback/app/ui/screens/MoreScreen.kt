package com.pushback.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pushback.app.ui.components.Eyebrow
import com.pushback.app.ui.components.Masthead
import com.pushback.app.ui.components.MoreRow
import com.pushback.app.ui.theme.PushBackTheme

@Composable
fun MoreScreen(
    onTasks: () -> Unit,
    onMorning: () -> Unit,
    onEvening: () -> Unit,
    onPatterns: () -> Unit,
    onMind: () -> Unit,
    onSettings: () -> Unit,
) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        Masthead("INDEX DISPATCH", "THE COMPLETE OPERATING SYSTEM")
        Spacer(Modifier.height(8.dp))
        Text("Operating Index", style = typography.title)
        Text(
            "The front page is Today. Everything in this index is how the front page stays honest and builds long-term leverage.",
            style = typography.bodySmall,
            color = colors.muted,
        )
        Spacer(Modifier.height(16.dp))

        MoreRow("Tasks & Debt Ledger", "Required protocol / compound block / optional tasks. Rolled debt tracking.") { onTasks() }
        MoreRow("Morning Briefing", "WOOP contrasting, designated Frog, and implementation intentions.") { onMorning() }
        MoreRow("Evening After-Action Review", "Supposed vs. happened vs. what went wrong. Calibrate instruments.") { onEvening() }
        MoreRow("Behavioral Patterns", "Mathematical analysis of your peak send hours and drift zones.") { onPatterns() }
        MoreRow("Mentality Vault", "Munger, Naval, Buffett, Bezos, Grove — axioms organized by failure mode.") { onMind() }
        MoreRow("Settings & Themes", "Press editions (Light/Dark/Sepia), quota goals, alarms, watchdog enemies.") { onSettings() }

        Spacer(Modifier.height(80.dp))
    }
}
