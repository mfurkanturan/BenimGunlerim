package com.benimgunlerim.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Katman 1 — Material şemasında karşılığı olmayan anlamsal roller.
 * Bileşenler `AppTheme.colors.progress.done` gibi rol adıyla okur; ham palete dokunmaz.
 */
@Immutable
data class ProgressColors(
    /** Yanan yıldız, güneş ışını, zincir noktası dolgusu */
    val done: Color,
    /** Yanan yıldızın kenarı */
    val doneEdge: Color,
    /** Kâğıt üzerinde ilerleme metni (AA kontrastlı) */
    val doneInk: Color,
    /** Henüz tamamlanmamış yıldız çizgisi */
    val idle: Color,
    /** Yumuşak ilerleme zemini ("kalan" kartı, rozet) */
    val soft: Color,
    /** Yumuşak zemindeki koyu metin */
    val onSoft: Color,
)

@Immutable
data class StatusTone(val fg: Color, val container: Color, val border: Color)

@Immutable
data class StatusColors(
    val success: StatusTone,
    /** Dikkat: uyarılar ve geciken işler — kırmızı değil, kayısı ailesi */
    val attention: StatusTone,
    val error: StatusTone,
    val info: StatusTone,
)

@Immutable
data class NightColors(
    val sky: Color,
    val card: Color,
    val line: Color,
    val moon: Color,
    val text: Color,
    val muted: Color,
    /** Gece ölçeği (ruh hali, enerji): sönükten aya doğru 5 ton */
    val scale: List<Color>,
)

@Immutable
data class RewardColors(
    val xp: Color,
    val xpSoft: Color,
    val xpBorder: Color,
    val streak: Color,
    val streakSoft: Color,
    val gold: Color,
    val goldSoft: Color,
    val onGold: Color,
)

@Immutable
data class CategoryColor(val fg: Color, val soft: Color)

@Immutable
data class AppColors(
    val progress: ProgressColors,
    val status: StatusColors,
    val night: NightColors,
    val reward: RewardColors,
    val category: List<CategoryColor>,
)

/** Kategori tonları — Composable dışından da (ör. CategoryPalette) okunabilir. */
val CategoryTones: List<CategoryColor> = listOf(
    CategoryColor(Palette.CatLeaf, Palette.Leaf50),
    CategoryColor(Palette.CatSea, Palette.Sea50),
    CategoryColor(Palette.CatPlum, Palette.Plum50),
    CategoryColor(Palette.CatBrick, Palette.Brick50),
    CategoryColor(Palette.CatMustard, Palette.Gold50),
    CategoryColor(Palette.CatStone, Palette.Paper200),
)

internal val LightAppColors = AppColors(
    progress = ProgressColors(
        done = Palette.Apricot500,
        doneEdge = Palette.Apricot600,
        doneInk = Palette.Apricot700,
        idle = Palette.Paper500,
        soft = Palette.Apricot50,
        onSoft = Palette.Apricot900,
    ),
    status = StatusColors(
        success = StatusTone(Palette.Leaf700, Palette.Leaf50, Palette.Leaf100),
        attention = StatusTone(Palette.Apricot700, Palette.Apricot50, Palette.Apricot100),
        error = StatusTone(Palette.Brick500, Palette.Brick50, Palette.Brick200),
        info = StatusTone(Palette.Sea700, Palette.Sea50, Palette.Sea200),
    ),
    night = NightColors(
        sky = Palette.Night900,
        card = Palette.Night700,
        line = Palette.Night400,
        moon = Palette.Moonlight,
        text = Palette.MoonText,
        muted = Palette.NightMuted,
        scale = listOf(Palette.Night400, Palette.Night300, Palette.NightMuted, Palette.Apricot200, Palette.Moonlight),
    ),
    reward = RewardColors(
        xp = Palette.Apricot700,
        xpSoft = Palette.Apricot50,
        xpBorder = Palette.Apricot100,
        streak = Palette.Apricot600,
        streakSoft = Palette.Apricot50,
        gold = Palette.Gold500,
        goldSoft = Palette.Gold50,
        onGold = Palette.Gold700,
    ),
    category = CategoryTones,
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

/** Tema erişim noktası: `AppTheme.colors.progress.done`, `MaterialTheme.colorScheme.primary` ile yan yana. */
object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable
        get() = LocalAppColors.current

    val scheme
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme
}
