package com.pushback.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pushback.app.domain.FailureMode
import com.pushback.app.domain.MentalityCard
import com.pushback.app.ui.components.Eyebrow
import com.pushback.app.ui.components.HairlineRule
import com.pushback.app.ui.components.Masthead
import com.pushback.app.ui.components.PaperSheet
import com.pushback.app.ui.components.PbButton
import com.pushback.app.ui.components.PbField
import com.pushback.app.ui.components.PbGhostButton
import com.pushback.app.ui.components.TrackChip
import com.pushback.app.ui.components.rememberTactileFeedback
import com.pushback.app.ui.theme.PushBackTheme

@Composable
fun MindScreen(cards: List<MentalityCard>) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography
    val feedback = rememberTactileFeedback(true)

    var searchQuery by remember { mutableStateOf("") }
    var selectedFailureMode by remember { mutableStateOf<FailureMode?>(null) }
    var selectedThinker by remember { mutableStateOf<String?>(null) }

    val thinkers = remember(cards) { cards.map { it.thinker }.distinct().sorted() }

    val filteredCards = cards.filter { card ->
        val matchesSearch = searchQuery.isBlank() ||
                card.line.contains(searchQuery, ignoreCase = true) ||
                card.thinker.contains(searchQuery, ignoreCase = true)
        val matchesMode = selectedFailureMode == null || card.failureMode == selectedFailureMode
        val matchesThinker = selectedThinker == null || card.thinker == selectedThinker
        matchesSearch && matchesMode && matchesThinker
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        item {
            Masthead("THE VAULT", "MENTALITY • DISCIPLINE • LEVERAGE")
            Spacer(Modifier.height(8.dp))
            Text("Mentality Vault", style = typography.title)
            Text(
                "These fire in context — avoidance, scrolling, fake work, no send, skipped review. Browsing them is allowed. Living on them is not the work.",
                style = typography.bodySmall,
                color = colors.muted,
            )
            Spacer(Modifier.height(14.dp))

            // Search Bar
            PbField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                hint = "Search axioms, thinkers...",
            )

            Spacer(Modifier.height(10.dp))

            // Filter by Failure Mode
            Eyebrow("DIAGNOSIS CATEGORY", colors.gold)
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    val allSelected = selectedFailureMode == null
                    Box(
                        Modifier
                            .background(if (allSelected) colors.tape else colors.surface)
                            .border(1.dp, if (allSelected) colors.night else colors.hairline)
                            .clickable {
                                feedback.click()
                                selectedFailureMode = null
                            }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Text("ALL MODES", style = typography.labelSmall.copy(color = if (allSelected) colors.tapeText else colors.muted))
                    }
                }
                items(FailureMode.entries) { mode ->
                    val isSelected = selectedFailureMode == mode
                    Box(
                        Modifier
                            .background(if (isSelected) colors.debt else colors.surface)
                            .border(1.dp, if (isSelected) colors.debt else colors.hairline)
                            .clickable {
                                feedback.click()
                                selectedFailureMode = if (isSelected) null else mode
                            }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Text(
                            mode.name.replace('_', ' '),
                            style = typography.labelSmall.copy(color = if (isSelected) colors.cream else colors.paper),
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Filter by Thinker
            Eyebrow("THINKER", colors.gold)
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    val allSelected = selectedThinker == null
                    Box(
                        Modifier
                            .background(if (allSelected) colors.tape else colors.surface)
                            .border(1.dp, if (allSelected) colors.night else colors.hairline)
                            .clickable {
                                feedback.click()
                                selectedThinker = null
                            }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Text("ALL THINKERS", style = typography.labelSmall.copy(color = if (allSelected) colors.tapeText else colors.muted))
                    }
                }
                items(thinkers) { thinker ->
                    val isSelected = selectedThinker == thinker
                    Box(
                        Modifier
                            .background(if (isSelected) colors.gold else colors.surface)
                            .border(1.dp, if (isSelected) colors.gold else colors.hairline)
                            .clickable {
                                feedback.click()
                                selectedThinker = if (isSelected) null else thinker
                            }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Text(
                            thinker,
                            style = typography.labelSmall.copy(color = if (isSelected) colors.cream else colors.paper),
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            HairlineRule()
            Spacer(Modifier.height(8.dp))
        }

        if (filteredCards.isEmpty()) {
            item {
                Spacer(Modifier.height(30.dp))
                Text("No mentality axioms matching filter.", style = typography.mono, color = colors.muted)
            }
        }

        items(filteredCards, key = { it.id }) { card ->
            MentalityCardItem(card)
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun MentalityCardItem(card: MentalityCard) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography
    val context = LocalContext.current
    val feedback = rememberTactileFeedback(true)

    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.padding(vertical = 6.dp)) {
        PaperSheet(borderColor = if (expanded) colors.gold else colors.hairline) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    card.thinker.uppercase(),
                    style = typography.label.copy(fontSize = 11.sp, color = colors.gold),
                )
                TrackChip(
                    label = card.failureMode.name.replace('_', ' '),
                    color = colors.debt,
                    filled = false,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "“${card.line}”",
                style = typography.pull.copy(fontSize = 18.sp, lineHeight = 25.sp),
                color = colors.paper,
            )

            if (expanded) {
                Spacer(Modifier.height(12.dp))
                HairlineRule()
                Spacer(Modifier.height(10.dp))
                Text("TACTICAL APPLICATION", style = typography.labelSmall, color = colors.gold)
                Spacer(Modifier.height(4.dp))
                Text(
                    when (card.failureMode) {
                        FailureMode.AVOIDANCE -> "Antidote: Close all tabs, open PushBack, and send listing #1 immediately."
                        FailureMode.FAKE_WORK -> "Antidote: Research does not equal pipeline. Count only confirmed submissions."
                        FailureMode.SCROLLING -> "Antidote: 5-minute cooldown. Lock the phone in another room until quota is cleared."
                        FailureMode.NO_SEND -> "Antidote: Lower the threshold. A brief, authentic application beats silence."
                        FailureMode.NO_REVIEW -> "Antidote: 8-minute after-action review before sleep to calibrate tomorrow."
                    },
                    style = typography.bodySmall,
                    color = colors.paper,
                )
                Spacer(Modifier.height(10.dp))
                PbGhostButton(
                    text = "Copy Axiom to Clipboard",
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    feedback.click()
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Mentality Axiom", "“${card.line}” — ${card.thinker}"))
                    Toast.makeText(context, "Axiom copied", Toast.LENGTH_SHORT).show()
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    if (expanded) "LESS ▲" else "DETAILS ▼",
                    style = typography.labelSmall.copy(color = colors.muted),
                    modifier = Modifier.clickable {
                        feedback.click()
                        expanded = !expanded
                    },
                )
            }
        }
    }
}
