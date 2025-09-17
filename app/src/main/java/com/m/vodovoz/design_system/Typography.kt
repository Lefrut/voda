package com.m.vodovoz.design_system

import androidx.compose.material3.Typography
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.m.vodovoz.R

private val baseTextStyle = TextStyle(
    lineHeightStyle = LineHeightStyle(
        LineHeightStyle.Alignment.Center,
        LineHeightStyle.Trim.None
    ),
    platformStyle = PlatformTextStyle(
        includeFontPadding = false
    )
)

val vodovozTypography = Typography(
    displayLarge = baseTextStyle.copy(
        fontSize = 50.sp,
        fontFamily = robotoFontFamily,
        fontWeight = FontWeight.Medium
    ),
    displaySmall = baseTextStyle.copy(
        fontSize = 32.sp,
        lineHeight = 40.sp,
        fontFamily = robotoFontFamily,
        fontWeight = FontWeight.Normal
    ),
    labelMedium = baseTextStyle.copy(
        fontSize = 13.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp,
        fontFamily = robotoFontFamily,
        fontWeight = FontWeight.Normal
    ),
    labelSmall = baseTextStyle.copy(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        fontFamily = robotoFontFamily,
        fontWeight = FontWeight.Normal
    ),
    labelLarge = baseTextStyle.copy(
        fontSize = 15.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp,
        fontFamily = robotoFontFamily,
        fontWeight = FontWeight.Normal
    ),
    titleLarge = baseTextStyle.copy(
        fontSize = 24.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.18.sp,
        fontFamily = robotoFontFamily,
        fontWeight = FontWeight.Medium
    ),
    titleMedium = baseTextStyle.copy(
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        fontFamily = robotoFontFamily,
        fontWeight = FontWeight.Medium
    ),
    titleSmall = baseTextStyle.copy(
        fontSize = 14.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp,
        fontFamily = robotoFontFamily,
        fontWeight = FontWeight.Medium
    ),
    headlineMedium = baseTextStyle.copy(
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        fontFamily = robotoFontFamily,
        fontWeight = FontWeight.Medium
    ),
    headlineSmall = baseTextStyle.copy(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        fontFamily = robotoFontFamily,
     
        fontWeight = FontWeight.Medium
    ),
    bodySmall = baseTextStyle.copy(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontFamily = robotoFontFamily,
     
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = baseTextStyle.copy(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.25.sp,
        fontFamily = robotoFontFamily,
     
        fontWeight = FontWeight.Normal
    ),
    bodyLarge = baseTextStyle.copy(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.25.sp,
        fontFamily = robotoFontFamily,
        fontWeight = FontWeight.Normal
    )
)

val extendedTypography = ExtendedTypography(
    buttonMedium = baseTextStyle.copy(
        fontSize = 17.sp,
        lineHeight = 24.sp,
        fontFamily = robotoFontFamily,
        letterSpacing = 0.15.sp,
        fontWeight = FontWeight.Medium
    ),
    buttonSmall = baseTextStyle.copy(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontFamily = robotoFontFamily,
        letterSpacing = 0.1.sp,
        fontWeight = FontWeight.Medium
    ),
    labelExtraSmall = baseTextStyle.copy(
        fontSize = 9.sp,
        lineHeight = 24.sp,
        fontFamily = robotoFontFamily,
        letterSpacing = 0.1.sp,
        fontWeight = FontWeight.Normal
    ),
    labelExtraSmallVariant = baseTextStyle.copy(
        fontSize = 10.sp,
        lineHeight = 16.sp,
        fontFamily = robotoFontFamily,
        letterSpacing = 0.4.sp,
        fontWeight = FontWeight.Normal
    ),
    labelMediumVariant = baseTextStyle.copy(
        fontSize = 12.sp,
        lineHeight = 24.sp,
        fontFamily = robotoFontFamily,
        letterSpacing = 0.1.sp,
        fontWeight = FontWeight.Medium
    ),
    labelSmallVariant = baseTextStyle.copy(
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontFamily = robotoFontFamily,
        letterSpacing = 0.25.sp,
        fontWeight = FontWeight.Normal
    )
)

class ExtendedTypography(
    val buttonMedium: TextStyle = TextStyle.Default,
    val buttonSmall: TextStyle = TextStyle.Default,
    val labelExtraSmall: TextStyle = TextStyle.Default,
    val labelExtraSmallVariant: TextStyle = TextStyle.Default,
    val labelMediumVariant: TextStyle = TextStyle.Default,
    val labelSmallVariant: TextStyle = TextStyle.Default,
)

val LocalExtendedTypography =
    staticCompositionLocalOf<ExtendedTypography> { error("extended typography didn't implement") }


val robotoFontFamily
    get() = FontFamily(
        Font(R.font.roboto_regular, FontWeight.Normal),
        Font(R.font.roboto_medium, FontWeight.Medium),
        Font(R.font.roboto_semibold, FontWeight.SemiBold),
        Font(R.font.roboto_bold, FontWeight.Bold),
    )