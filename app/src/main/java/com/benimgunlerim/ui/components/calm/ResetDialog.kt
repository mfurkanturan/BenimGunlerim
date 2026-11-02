package com.benimgunlerim.ui.components.calm

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import com.benimgunlerim.ui.theme.AppTheme
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.NightSurfaceTheme

/** Reset sonunda önerilen tek adım: bugünün açık bir görevi ya da rutini. */
data class ResetCandidate(val id: String, val title: String, val isRoutine: Boolean)

internal enum class ResetStage { Intro, Breathing, OneStep }

/**
 * Reset: Dur → Nefes al → Tek adım. Gece göğü sayfası; nefes süresi gerçek zamana göre işler
 * (kayma yok), sonunda bugünün açık hedeflerinden biri "şimdi sadece bu" diye seçilir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetDialog(
    onDismiss: () -> Unit,
    onEnableLightDay: () -> Unit,
    candidates: List<ResetCandidate>,
    onPickStep: (ResetCandidate) -> Unit,
    isLightDay: Boolean = false,
    initialDurationSeconds: Int = 60,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var stage by rememberSaveable { mutableStateOf(ResetStage.Intro) }
    var durationSeconds by rememberSaveable { mutableIntStateOf(initialDurationSeconds) }
    var elapsedMs by rememberSaveable { mutableLongStateOf(0L) }
    val night = AppTheme.colors.night

    LaunchedEffect(stage) {
        if (stage != ResetStage.Breathing) return@LaunchedEffect
        val totalMs = durationSeconds * 1_000L
        val resumeFrom = elapsedMs
        val start = withFrameMillis { it }
        while (elapsedMs < totalMs) {
            elapsedMs = resumeFrom + (withFrameMillis { it } - start)
        }
        stage = ResetStage.OneStep
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = night.sky,
        contentColor = night.text,
        dragHandle = { BottomSheetDefaults.DragHandle(color = night.muted) },
    ) {
        NightSurfaceTheme {
            AnimatedContent(
                targetState = stage,
                transitionSpec = { fadeIn(tween(AppTokens.Motion.normal)) togetherWith fadeOut(tween(AppTokens.Motion.fast)) },
                label = "reset_stage",
            ) { current ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppTokens.Spacing.xl)
                        .padding(bottom = AppTokens.Spacing.md)
                        .navigationBarsPadding(),
                ) {
                    when (current) {
                        ResetStage.Intro -> ResetIntro(
                            durationSeconds = durationSeconds,
                            onDurationSelect = { durationSeconds = it },
                            onStart = {
                                elapsedMs = 0L
                                stage = ResetStage.Breathing
                            },
                        )
                        ResetStage.Breathing -> ResetBreathing(
                            elapsedMs = elapsedMs,
                            totalMs = durationSeconds * 1_000L,
                            onEnough = { stage = ResetStage.OneStep },
                        )
                        ResetStage.OneStep -> ResetOneStep(
                            candidates = candidates,
                            isLightDay = isLightDay,
                            onPick = {
                                onPickStep(it)
                                onDismiss()
                            },
                            onLightDay = {
                                onEnableLightDay()
                                onDismiss()
                            },
                            onDone = onDismiss,
                        )
                    }
                }
            }
        }
    }
}
