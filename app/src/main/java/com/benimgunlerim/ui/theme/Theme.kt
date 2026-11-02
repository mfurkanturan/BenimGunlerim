@file:Suppress("SpellCheckingInspection")
@file:OptIn(ExperimentalTextApi::class)
package com.benimgunlerim.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.benimgunlerim.R

// ── Nunito Font Family ────────────────────────────────────────────────────────
private fun nunito(weight: FontWeight) = Font(
    R.font.nunito,
    weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

// Nunito dosyası değişken fonttur; kalınlık FontVariation ile seçilir.
val Nunito = FontFamily(
    nunito(FontWeight.Normal),
    nunito(FontWeight.Medium),
    nunito(FontWeight.SemiBold),
    nunito(FontWeight.Bold),
    nunito(FontWeight.ExtraBold),
)

// ── Bricolage Grotesque (başlık fontu, değişken; OFL) ─────────────────────────
private fun bricolage(weight: FontWeight) = Font(
    R.font.bricolage_grotesque,
    weight,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight.weight),
        FontVariation.Setting("opsz", 48f),
    ),
)

val BricolageGrotesque = FontFamily(
    bricolage(FontWeight.Bold),
    bricolage(FontWeight.ExtraBold),
)

// ═════════════════════════════════════════════════════════════════════════════
//  Renk — Katman 2: Material şeması (ham palet: ColorPrimitives.kt, ek roller: AppColors.kt)
// ═════════════════════════════════════════════════════════════════════════════

private val LightColors = lightColorScheme(
    primary              = Palette.Leaf700,
    onPrimary            = Palette.Paper50,
    primaryContainer     = Palette.Leaf50,
    onPrimaryContainer   = Palette.Leaf900,
    secondary            = Palette.Apricot500,
    onSecondary          = Palette.Ink,
    secondaryContainer   = Palette.Apricot50,
    onSecondaryContainer = Palette.Apricot900,
    tertiary             = Palette.Night900,
    onTertiary           = Palette.MoonText,
    tertiaryContainer    = Palette.Night700,
    onTertiaryContainer  = Palette.Moonlight,
    background           = Palette.Paper100,
    surface              = Palette.Paper50,
    surfaceVariant       = Palette.Paper200,
    surfaceContainer     = Palette.Paper50,
    surfaceContainerHigh = Palette.Paper200,
    surfaceContainerLow  = Palette.Paper100,
    surfaceContainerLowest = Palette.Paper50,
    surfaceContainerHighest = Palette.Paper300,
    onBackground         = Palette.Ink,
    onSurface            = Palette.Ink,
    onSurfaceVariant     = Palette.Paper700,
    outline              = Palette.Paper400,
    outlineVariant       = Palette.Paper300,
    error                = Palette.Brick500,
    onError              = Palette.Paper50,
    errorContainer       = Palette.Brick50,
    onErrorContainer     = Palette.Brick700,
    scrim                = Palette.Ink,
)

// ── Tipografi ─────────────────────────────────────────────────────────────────
private val AppPlatformTextStyle = PlatformTextStyle(includeFontPadding = false)

private val AppTypography = Typography(
    displayLarge   = TextStyle(fontFamily = BricolageGrotesque, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.ExtraBold, fontSize = 40.sp, lineHeight = 42.sp, letterSpacing = (-0.8).sp),
    displayMedium  = TextStyle(fontFamily = BricolageGrotesque, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 38.sp, letterSpacing = (-0.6).sp),
    headlineLarge  = TextStyle(fontFamily = BricolageGrotesque, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, lineHeight = 30.sp, letterSpacing = (-0.3).sp),
    headlineMedium = TextStyle(fontFamily = BricolageGrotesque, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, lineHeight = 26.sp, letterSpacing = (-0.2).sp),
    headlineSmall  = TextStyle(fontFamily = Nunito, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.SemiBold,  fontSize = 18.sp, lineHeight = 26.sp),
    titleLarge     = TextStyle(fontFamily = Nunito, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.SemiBold,  fontSize = 18.sp, lineHeight = 26.sp),
    titleMedium    = TextStyle(fontFamily = Nunito, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.SemiBold,  fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.1.sp),
    titleSmall     = TextStyle(fontFamily = Nunito, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.Medium,    fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge      = TextStyle(fontFamily = Nunito, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.Normal,    fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontFamily = Nunito, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.Normal,    fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall      = TextStyle(fontFamily = Nunito, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.Normal,    fontSize = 12.sp, lineHeight = 16.8.sp),
    labelLarge     = TextStyle(fontFamily = Nunito, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.SemiBold,  fontSize = 14.sp, letterSpacing = 0.1.sp),
    labelMedium    = TextStyle(fontFamily = Nunito, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.Medium,    fontSize = 12.sp, letterSpacing = 0.3.sp),
    labelSmall     = TextStyle(fontFamily = Nunito, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.SemiBold,  fontSize = 10.sp, letterSpacing = 0.3.sp),
)

// ── Shape seti (Radius token'larından türetilmiş) ─────────────────────────────
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(AppTokens.Radius.xs),   // 4dp
    small      = RoundedCornerShape(AppTokens.Radius.sm),   // 8dp
    medium     = RoundedCornerShape(AppTokens.Radius.md),   // 12dp
    large      = RoundedCornerShape(AppTokens.Radius.lg),   // 16dp
    extraLarge = RoundedCornerShape(AppTokens.Radius.xl),   // 20dp
)

// ── Tema giriş noktası ────────────────────────────────────────────────────────
@Composable
fun BenimGunlerimTheme(
    content: @Composable () -> Unit,
) {
    // Karanlık mod devre dışı bırakıldı — uygulama tutarlı ve temiz açık modda çalışır.
    CompositionLocalProvider(LocalAppColors provides LightAppColors) {
        MaterialTheme(
            colorScheme = LightColors,
            typography  = AppTypography,
            shapes      = AppShapes,
            content     = content,
        )
    }
}

// ── Gece yüzeyi (karanlık mod DEĞİL) ─────────────────────────────────────────
// Sadece "Günü kapat" gibi tasarımın gece sahnesi olarak tanımladığı yüzeylerde, o alt ağaca
// yerel olarak uygulanır. Uygulamanın teması her zaman LightColors'tur.
private val NightSurfaceColors = LightColors.copy(
    primary              = Palette.Moonlight,
    onPrimary            = Palette.Night900,
    background           = Palette.Night900,
    surface              = Palette.Night900,
    surfaceVariant       = Palette.Night700,
    surfaceContainer     = Palette.Night900,
    surfaceContainerHigh = Palette.Night700,
    surfaceContainerLow  = Palette.Night900,
    onBackground         = Palette.MoonText,
    onSurface            = Palette.MoonText,
    onSurfaceVariant     = Palette.NightMuted,
    outline              = Palette.Night400,
    outlineVariant       = Palette.Night400,
)

@Composable
fun NightSurfaceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NightSurfaceColors,
        typography  = AppTypography,
        shapes      = AppShapes,
        content     = content,
    )
}
