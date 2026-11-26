package com.pushback.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pushback.app.ui.theme.Cream
import com.pushback.app.ui.theme.Debt
import com.pushback.app.ui.theme.Gold
import com.pushback.app.ui.theme.Good
import com.pushback.app.ui.theme.Hairline
import com.pushback.app.ui.theme.Ink
import com.pushback.app.ui.theme.Muted
import com.pushback.app.ui.theme.Night
import com.pushback.app.ui.theme.Paper
import com.pushback.app.ui.theme.PbTypography
import com.pushback.app.ui.theme.PushBackTheme
import com.pushback.app.ui.theme.Surface
import com.pushback.app.ui.theme.Tape

@Composable
fun RuledPaper(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val colors = PushBackTheme.colors
    Box(
        modifier
            .fillMaxSize()
            .background(colors.ink)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val step = 28.dp.toPx()
            var y = step
            while (y < size.height) {
                drawLine(
                    color = colors.hairline.copy(alpha = if (colors.isDark) 0.25f else 0.45f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f,
                )
                y += step
            }
            // Vertical margin rule
            drawLine(
                color = colors.debt.copy(alpha = if (colors.isDark) 0.35f else 0.22f),
                start = Offset(24.dp.toPx(), 0f),
                end = Offset(24.dp.toPx(), size.height),
                strokeWidth = 1.5f,
            )
        }
        content()
    }
}

@Composable
fun Masthead(
    kicker: String,
    date: String,
    modifier: Modifier = Modifier,
    volume: String = "VOL. I",
    issue: String = "NO. 42",
) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography

    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                volume,
                style = typography.labelSmall,
                color = colors.muted,
            )
            Text(
                "★ DAILY DISPATCH ★",
                style = typography.labelSmall,
                color = colors.gold,
            )
            Text(
                issue,
                style = typography.labelSmall,
                color = colors.muted,
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Text(
                "PUSHBACK",
                style = typography.titleLarge.copy(
                    fontSize = 32.sp,
                    letterSpacing = 4.sp,
                    color = colors.paper,
                ),
                modifier = Modifier.weight(1f),
            )
            Text(
                date.uppercase(),
                style = typography.monoBold.copy(fontSize = 11.sp),
                color = colors.muted,
            )
        }
        Spacer(Modifier.height(6.dp))
        // Classic broadsheet double rule
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(colors.paper)
        )
        Box(
            Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
                .height(1.dp)
                .background(colors.paper)
        )
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(kicker.uppercase(), style = typography.label, color = colors.debt)
            Box(
                Modifier
                    .background(colors.tape.copy(alpha = 0.2f))
                    .border(1.dp, colors.tape)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    "ON-DEVICE OS",
                    style = typography.labelSmall.copy(color = colors.gold),
                )
            }
        }
    }
}

@Composable
fun PaperSheet(
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    backgroundColor: Color? = null,
    content: @Composable () -> Unit,
) {
    val colors = PushBackTheme.colors
    val finalBorder = borderColor ?: colors.paper
    val finalBg = backgroundColor ?: colors.surface

    Box(modifier.fillMaxWidth()) {
        // Shadow plate
        Box(
            Modifier
                .matchParentSize()
                .offset(x = 3.dp, y = 3.dp)
                .background(colors.night.copy(alpha = if (colors.isDark) 0.5f else 0.18f)),
        )
        Column(
            Modifier
                .fillMaxWidth()
                .background(finalBg)
                .border(1.2.dp, finalBorder)
                .padding(16.dp),
        ) {
            content()
        }
    }
}

@Composable
fun StampDigit(
    value: String,
    label: String,
    hot: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography
    val stampColor = if (hot) colors.debt else colors.good

    val scale = remember { Animatable(1f) }
    val rotation = remember { Animatable(-7f) }

    LaunchedEffect(value, hot) {
        scale.snapTo(1.25f)
        rotation.snapTo(if (hot) -9f else -5f)
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        rotation.animateTo(if (hot) -7f else -4f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
    }

    Box(
        modifier = modifier
            .size(200.dp)
            .scale(scale.value)
            .rotate(rotation.value)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.matchParentSize()) {
            val strokeWidth = 5.dp.toPx()
            val stroke = Stroke(
                width = strokeWidth,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 8f), 2f),
            )
            // Outer dashed stamp ring
            drawCircle(
                color = stampColor,
                style = stroke,
            )
            // Inner subtle solid ring
            drawCircle(
                color = stampColor.copy(alpha = 0.5f),
                radius = size.minDimension / 2f - 8.dp.toPx(),
                style = Stroke(width = 1.5.dp.toPx()),
            )
            // Ink tint wash
            drawCircle(
                color = stampColor.copy(alpha = if (colors.isDark) 0.15f else 0.08f),
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp),
        ) {
            Text(
                value,
                style = typography.display.copy(
                    color = stampColor,
                    fontSize = if (value.length > 3) 44.sp else if (value.length > 2) 58.sp else 84.sp,
                    lineHeight = if (value.length > 3) 44.sp else if (value.length > 2) 58.sp else 78.sp,
                ),
            )
            Spacer(Modifier.height(2.dp))
            Box(
                Modifier
                    .background(stampColor)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    label.uppercase(),
                    style = typography.label.copy(color = colors.cream, fontSize = 9.sp),
                )
            }
        }
    }
}

@Composable
fun PbButton(
    text: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
    textColor: Color? = null,
    onClick: () -> Unit,
) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography
    val feedback = rememberTactileFeedback(true)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "btnScale",
    )

    val btnBg = color ?: colors.debt
    val btnText = textColor ?: colors.cream

    Box(
        modifier
            .scale(animatedScale)
            .background(btnBg)
            .border(1.dp, colors.night.copy(alpha = 0.3f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    feedback.click()
                    onClick()
                },
            )
            .padding(horizontal = 18.dp, vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text.uppercase(),
            style = typography.label.copy(
                color = btnText,
                letterSpacing = 2.sp,
                fontSize = 11.sp,
            ),
        )
    }
}

@Composable
fun PbGhostButton(
    text: String,
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    textColor: Color? = null,
    onClick: () -> Unit,
) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography
    val feedback = rememberTactileFeedback(true)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "ghostBtnScale",
    )

    val bColor = borderColor ?: colors.paper
    val tColor = textColor ?: colors.paper

    Box(
        modifier
            .scale(animatedScale)
            .background(colors.surface.copy(alpha = 0.4f))
            .border(1.2.dp, bColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    feedback.click()
                    onClick()
                },
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text.uppercase(),
            style = typography.label.copy(
                color = tColor,
                letterSpacing = 1.8.sp,
                fontSize = 10.5.sp,
            ),
        )
    }
}

@Composable
fun Eyebrow(
    text: String,
    color: Color? = null,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography
    val activeColor = color ?: colors.gold

    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(9.dp)
                    .background(colors.tape)
                    .border(1.dp, colors.night)
            )
            Spacer(Modifier.width(8.dp))
            Text(text.uppercase(), style = typography.label, color = activeColor)
        }
        if (actionText != null && onAction != null) {
            Text(
                actionText.uppercase(),
                style = typography.labelSmall.copy(color = colors.debt),
                modifier = Modifier.clickable(onClick = onAction),
            )
        }
    }
}

@Composable
fun HairlineRule(modifier: Modifier = Modifier) {
    val colors = PushBackTheme.colors
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colors.hairline.copy(alpha = 0.6f))
    )
}

@Composable
fun DoubleHairlineRule(modifier: Modifier = Modifier) {
    val colors = PushBackTheme.colors
    Column(modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(colors.paper)
        )
        Spacer(Modifier.height(2.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(0.8.dp)
                .background(colors.paper)
        )
    }
}

@Composable
fun PbField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    maxLines: Int = 5,
) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = typography.body.copy(color = colors.paper),
        cursorBrush = SolidColor(colors.debt),
        minLines = minLines,
        maxLines = maxLines,
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceElevated)
            .border(1.2.dp, colors.paper)
            .padding(14.dp),
        decorationBox = { inner ->
            Box {
                if (value.isEmpty()) {
                    Text(hint, style = typography.body, color = colors.muted)
                }
                inner()
            }
        },
    )
}

@Composable
fun TrackChip(label: String, color: Color, filled: Boolean = false) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography

    Text(
        label.uppercase(),
        style = typography.labelSmall.copy(color = if (filled) colors.cream else color),
        modifier = Modifier
            .background(if (filled) color else colors.surface)
            .border(1.dp, color)
            .padding(horizontal = 7.dp, vertical = 3.5.dp),
    )
}

@Composable
fun StatCell(label: String, value: String, accent: Color? = null, modifier: Modifier = Modifier) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography
    val finalAccent = accent ?: colors.paper

    Column(modifier.padding(end = 12.dp)) {
        Text(value, style = typography.title.copy(fontSize = 24.sp, color = finalAccent))
        Spacer(Modifier.height(2.dp))
        Text(label.uppercase(), style = typography.labelSmall, color = colors.muted)
    }
}

@Composable
fun MoreRow(title: String, subtitle: String, onClick: () -> Unit) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography
    val feedback = rememberTactileFeedback(true)

    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                feedback.click()
                onClick()
            })
            .padding(vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                style = typography.title.copy(fontSize = 22.sp),
                color = colors.paper,
                modifier = Modifier.weight(1f),
            )
            Text("→", style = typography.monoBold.copy(fontSize = 16.sp), color = colors.muted)
        }
        Spacer(Modifier.height(3.dp))
        Text(subtitle, style = typography.bodySmall, color = colors.muted)
        Spacer(Modifier.height(10.dp))
        HairlineRule()
    }
}

@Composable
fun NewspaperClippingCard(
    quote: String,
    author: String,
    category: String = "MENTALITY",
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography

    PaperSheet(
        modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        borderColor = colors.gold,
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Eyebrow(category, colors.gold)
            Text("AXIOM", style = typography.labelSmall, color = colors.muted)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "“$quote”",
            style = typography.pull.copy(fontSize = 17.sp, lineHeight = 24.sp),
            color = colors.paper,
        )
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "— $author".uppercase(),
                style = typography.label.copy(fontSize = 9.5.sp, color = colors.gold),
            )
        }
    }
}

/**
 * Visual Canvas-drawn Heatmap Grid for 30 days
 */
@Composable
fun HeatmapGrid(
    dayHits: List<Pair<String, Boolean?>>, // date to hit status (true=hit, false=missed, null=no data)
    modifier: Modifier = Modifier,
) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography

    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Eyebrow("30-DAY RUNWAY HEATMAP", colors.gold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(colors.good))
                    Spacer(Modifier.width(4.dp))
                    Text("HIT", style = typography.labelSmall, color = colors.muted)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(colors.debt))
                    Spacer(Modifier.width(4.dp))
                    Text("MISS", style = typography.labelSmall, color = colors.muted)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            val totalSlots = 30
            val cols = 10
            val rows = 3
            val gap = 4.dp.toPx()
            val cellWidth = (size.width - (cols - 1) * gap) / cols
            val cellHeight = (size.height - (rows - 1) * gap) / rows
            val corner = CornerRadius(2.dp.toPx(), 2.dp.toPx())

            for (i in 0 until totalSlots) {
                val col = i % cols
                val row = i / cols
                val x = col * (cellWidth + gap)
                val y = row * (cellHeight + gap)

                val item = dayHits.getOrNull(i)
                val status = item?.second
                val cellColor = when (status) {
                    true -> colors.good
                    false -> colors.debt
                    null -> colors.hairline.copy(alpha = 0.35f)
                }

                drawRoundRect(
                    color = cellColor,
                    topLeft = Offset(x, y),
                    size = Size(cellWidth, cellHeight),
                    cornerRadius = corner,
                )
            }
        }
    }
}

/**
 * Scrolling Marquee / Ticker Tape
 */
@Composable
fun TickerTapeBanner(
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography

    Row(
        modifier
            .fillMaxWidth()
            .background(colors.tape)
            .border(1.dp, colors.night)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("⚡", fontSize = 11.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            text.uppercase(),
            style = typography.label.copy(
                color = colors.tapeText,
                fontSize = 9.5.sp,
                letterSpacing = 1.6.sp,
            ),
            maxLines = 1,
        )
    }
}

@Composable
fun ScreenPad(content: @Composable () -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) { content() }
}

@Composable
fun SpacerW(w: Int) = Spacer(Modifier.width(w.dp))
