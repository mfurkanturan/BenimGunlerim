package com.benimgunlerim.ui.components.calm

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.benimgunlerim.ui.theme.BenimGunlerimTheme

@Preview(name = "Nefes alma", showBackground = true, backgroundColor = 0xFF14213D)
@Composable
fun BreathingMoonInhalePreview() {
    BenimGunlerimTheme { BreathingMoon(elapsedMs = 3_000L, totalMs = 60_000L) }
}

@Preview(name = "Nefes verme", showBackground = true, backgroundColor = 0xFF14213D)
@Composable
fun BreathingMoonExhalePreview() {
    BenimGunlerimTheme { BreathingMoon(elapsedMs = 30_000L + 7_000L, totalMs = 60_000L) }
}
