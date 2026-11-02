package com.benimgunlerim.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import java.time.LocalTime

/** Günün dört hali; Bugün ekranının göğü ve selamlaması buna göre değişir. */
enum class DayPhase { Sabah, Ogle, Aksam, Gece }

private val MorningStart: LocalTime = LocalTime.of(5, 0)
private val NoonStart: LocalTime = LocalTime.of(11, 0)
private val EveningStart: LocalTime = LocalTime.of(16, 30)
private val NightStart: LocalTime = LocalTime.of(20, 30)

fun dayPhaseFor(time: LocalTime): DayPhase = when {
    time < MorningStart -> DayPhase.Gece
    time < NoonStart -> DayPhase.Sabah
    time < EveningStart -> DayPhase.Ogle
    time < NightStart -> DayPhase.Aksam
    else -> DayPhase.Gece
}

/**
 * [sunY] güneşin/ayın gök yüksekliğine oranla merkezi (0 = üst, 1 = alt).
 * [onSky] gök üstündeki metin rengi; [onSkyMuted] ikincil metin. Hepsi gök rengi üzerinde ≥4.5:1.
 */
@Immutable
data class SkyPalette(
    val sky: Color,
    val sun: Color,
    val sunY: Float,
    val isMoon: Boolean,
    val hillBack: Color,
    val hillFront: Color,
    val onSky: Color,
    val onSkyMuted: Color,
    val chip: Color,
    val starLit: Color,
    val starIdle: Color,
)

fun skyPaletteFor(phase: DayPhase): SkyPalette = when (phase) {
    DayPhase.Sabah -> SkyPalette(
        sky = Palette.SkyMorning, sun = Color(0xFFFFF4D6), sunY = 0.46f, isMoon = false,
        hillBack = Color(0xFF6E9A6B), hillFront = Color(0xFF3F6F4C),
        onSky = Color(0xFF2B2410), onSkyMuted = Color(0xFF4F4320), chip = Color(0xFFF9E8C4),
        starLit = Palette.Apricot500, starIdle = Color(0xFF8A6A2A),
    )
    DayPhase.Ogle -> SkyPalette(
        sky = Palette.SkyNoon, sun = Color(0xFFFFF7E0), sunY = 0.40f, isMoon = false,
        hillBack = Color(0xFF5E9270), hillFront = Color(0xFF2F6A4D),
        onSky = Color(0xFF14262B), onSkyMuted = Color(0xFF2E4A52), chip = Color(0xFFD7EAEE),
        starLit = Color(0xFFE9A23B), starIdle = Color(0xFF3F6470),
    )
    DayPhase.Aksam -> SkyPalette(
        sky = Palette.SkyEvening, sun = Color(0xFFFFE9CC), sunY = 0.56f, isMoon = false,
        hillBack = Color(0xFF2E6B53), hillFront = Color(0xFF173F31),
        onSky = Color(0xFF1E2420), onSkyMuted = Color(0xFF3A2210), chip = Color(0xFFF7D3B0),
        starLit = Palette.Apricot600, starIdle = Color(0xFF8A4A1C),
    )
    DayPhase.Gece -> SkyPalette(
        sky = Palette.Night900, sun = Color(0xFFF5E6C8), sunY = 0.30f, isMoon = true,
        hillBack = Color(0xFF22344F), hillFront = Color(0xFF16253A),
        onSky = Palette.MoonText, onSkyMuted = Palette.NightMuted, chip = Palette.Night700,
        starLit = Palette.Moonlight, starIdle = Palette.Night300,
    )
}

/** Sadece debug derlemede: gökyüzü fazını elle sabitlemek için (adb intent extra "debug_sky_phase"). */
object DebugSkyPhase {
    @Volatile var override: DayPhase? = null

    /** Bugün ekranını boş gün olarak göster (davet kartını görmek için; adb extra "debug_empty_today"). */
    @Volatile var forceEmptyToday: Boolean = false

    /** Plan'ı örnek görevlerle dolu göster; gerçek veriye dokunmaz (adb extra "debug_sample_plan"). */
    @Volatile var forceSamplePlan: Boolean = false

    /** Firebase yapılandırılmadan hesap ekranlarını görmek için (adb extra "debug_account_ui"). */
    @Volatile var forceAccountUi: Boolean = false

    /** Canlı Remote Config'e dokunmadan güncelleme pop-up'ını görmek için: "optional" / "required" (adb extra "debug_update_prompt"). */
    @Volatile var forceUpdatePrompt: String? = null
}
