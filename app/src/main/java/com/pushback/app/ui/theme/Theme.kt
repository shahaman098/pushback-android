package com.pushback.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pushback.app.R

/**
 * Standard palette constants for direct access
 */
val Ink = Color(0xFFE7DCC4)
val Paper = Color(0xFF1C140E)
val Surface = Color(0xFFF6EFE0)
val Hairline = Color(0xFFC9BFA8)
val Muted = Color(0xFF6F6658)
val Debt = Color(0xFFC1121F)
val Gold = Color(0xFF1D3557)
val Good = Color(0xFF2A6F4E)
val Night = Color(0xFF16120C)
val Tape = Color(0xFFF4D35E)
val Cream = Color(0xFFF7F1E3)

data class PbColors(
    val ink: Color,
    val paper: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val hairline: Color,
    val muted: Color,
    val debt: Color,
    val gold: Color,
    val good: Color,
    val night: Color,
    val tape: Color,
    val tapeText: Color,
    val cream: Color,
    val isDark: Boolean = false,
)

val MorningColors = PbColors(
    ink = Color(0xFFE7DCC4),
    paper = Color(0xFF1C140E),
    surface = Color(0xFFF6EFE0),
    surfaceElevated = Color(0xFFFCF9F2),
    hairline = Color(0xFFC9BFA8),
    muted = Color(0xFF6F6658),
    debt = Color(0xFFC1121F),
    gold = Color(0xFF1D3557),
    good = Color(0xFF2A6F4E),
    night = Color(0xFF16120C),
    tape = Color(0xFFF4D35E),
    tapeText = Color(0xFF16120C),
    cream = Color(0xFFF7F1E3),
    isDark = false,
)

val MidnightColors = PbColors(
    ink = Color(0xFF0F0D0B),
    paper = Color(0xFFF0EBE1),
    surface = Color(0xFF1B1612),
    surfaceElevated = Color(0xFF241F1A),
    hairline = Color(0xFF383129),
    muted = Color(0xFF9E9284),
    debt = Color(0xFFFF3B4E),
    gold = Color(0xFFE5A93C),
    good = Color(0xFF34D399),
    night = Color(0xFF070605),
    tape = Color(0xFFE5A93C),
    tapeText = Color(0xFF120E09),
    cream = Color(0xFF1E1914),
    isDark = true,
)

val SepiaColors = PbColors(
    ink = Color(0xFFDFD3C3),
    paper = Color(0xFF2C1E16),
    surface = Color(0xFFEBE2D5),
    surfaceElevated = Color(0xFFF4EDE4),
    hairline = Color(0xFFB8A995),
    muted = Color(0xFF706155),
    debt = Color(0xFFA82B24),
    gold = Color(0xFF3E4E5B),
    good = Color(0xFF2E6347),
    night = Color(0xFF1C130D),
    tape = Color(0xFFD4A373),
    tapeText = Color(0xFF24160E),
    cream = Color(0xFFEBE2D5),
    isDark = false,
)

private val Display = FontFamily(
    Font(R.font.newsreader_semibold, FontWeight.SemiBold),
    Font(R.font.newsreader_bold, FontWeight.Bold),
)
private val Sans = FontFamily(
    Font(R.font.publicsans_regular, FontWeight.Normal),
    Font(R.font.publicsans_semibold, FontWeight.SemiBold),
)
private val Mono = FontFamily(Font(R.font.splinemono_regular, FontWeight.Normal))

data class PbType(
    val display: TextStyle,
    val title: TextStyle,
    val titleLarge: TextStyle,
    val body: TextStyle,
    val bodySmall: TextStyle,
    val label: TextStyle,
    val labelSmall: TextStyle,
    val mono: TextStyle,
    val monoBold: TextStyle,
    val pull: TextStyle,
)

fun createTypography(colors: PbColors) = PbType(
    display = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.Bold,
        fontSize = 88.sp,
        lineHeight = 82.sp,
        letterSpacing = (-3).sp,
        color = colors.paper,
    ),
    title = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.6).sp,
        color = colors.paper,
    ),
    titleLarge = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.Bold,
        fontSize = 38.sp,
        lineHeight = 42.sp,
        letterSpacing = (-1).sp,
        color = colors.paper,
    ),
    body = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = colors.paper,
    ),
    bodySmall = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        lineHeight = 19.sp,
        color = colors.muted,
    ),
    label = TextStyle(
        fontFamily = Mono,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        letterSpacing = 2.2.sp,
        color = colors.gold,
    ),
    labelSmall = TextStyle(
        fontFamily = Mono,
        fontWeight = FontWeight.Normal,
        fontSize = 8.5.sp,
        letterSpacing = 1.8.sp,
        color = colors.muted,
    ),
    mono = TextStyle(
        fontFamily = Mono,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        color = colors.paper,
    ),
    monoBold = TextStyle(
        fontFamily = Mono,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        color = colors.paper,
    ),
    pull = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.SemiBold,
        fontStyle = FontStyle.Italic,
        fontSize = 19.sp,
        lineHeight = 25.sp,
        color = colors.paper,
    ),
)

val PbTypography = createTypography(MorningColors)

val LocalPbColors = staticCompositionLocalOf { MorningColors }
val LocalPbType = staticCompositionLocalOf { PbTypography }

object PushBackTheme {
    val colors: PbColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPbColors.current

    val typography: PbType
        @Composable
        @ReadOnlyComposable
        get() = LocalPbType.current
}

@Composable
fun PbTheme(
    themeMode: String = "MORNING",
    content: @Composable () -> Unit,
) {
    val colors = when (themeMode.uppercase()) {
        "MIDNIGHT" -> MidnightColors
        "SEPIA" -> SepiaColors
        else -> MorningColors
    }
    val typography = createTypography(colors)

    val colorScheme = if (colors.isDark) {
        darkColorScheme(
            background = colors.ink,
            surface = colors.surface,
            primary = colors.debt,
            onPrimary = colors.paper,
            onBackground = colors.paper,
            onSurface = colors.paper,
            secondary = colors.gold,
        )
    } else {
        lightColorScheme(
            background = colors.ink,
            surface = colors.surface,
            primary = colors.debt,
            onPrimary = colors.cream,
            onBackground = colors.paper,
            onSurface = colors.paper,
            secondary = colors.gold,
        )
    }

    CompositionLocalProvider(
        LocalPbColors provides colors,
        LocalPbType provides typography,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}
