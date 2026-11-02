package com.benimgunlerim.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.molecules.SheetSaveButton
import com.benimgunlerim.ui.theme.AppTokens

private const val StepCount = 5

/**
 * Onboarding bir gün doğumu: gece tanışma → şafakta ihtiyaç → sabah tempo → ilk adımlar → öğlen "gökyüzün hazır".
 * Üstteki gök her adımda biraz daha aydınlanır; Mino her adımda bir cümle söyler.
 */
@Composable
fun OnboardingScreen(onComplete: (String, String, List<String>, String?, String) -> Unit) {
    val context = LocalContext.current
    val needOptions = remember {
        listOf(
            NeedOption("duzen", R.string.onboarding_need_organize, R.string.onboarding_need_organize_sub, Icons.Rounded.WbSunny),
            NeedOption("duzenli", R.string.onboarding_need_regular, R.string.onboarding_need_regular_sub, Icons.Rounded.CalendarMonth),
            NeedOption("saglik", R.string.onboarding_need_health, R.string.onboarding_need_health_sub, Icons.Rounded.WaterDrop),
            NeedOption("odak", R.string.onboarding_need_focus, R.string.onboarding_need_focus_sub, Icons.Rounded.CenterFocusStrong),
            NeedOption("basit", R.string.onboarding_need_simple, R.string.onboarding_need_simple_sub, Icons.Rounded.EditNote),
        )
    }
    val intensityOptions = remember {
        listOf(
            IntensityOption("hafif", R.string.onboarding_intensity_light, R.string.onboarding_intensity_light_sub, Icons.Rounded.Spa, recommended = true),
            IntensityOption("dengeli", R.string.onboarding_intensity_balanced, R.string.onboarding_intensity_balanced_sub, Icons.Rounded.SelfImprovement),
            IntensityOption("yogun", R.string.onboarding_intensity_intense, R.string.onboarding_intensity_intense_sub, Icons.Rounded.LocalFireDepartment),
        )
    }

    var step by rememberSaveable { mutableIntStateOf(0) }
    var userName by rememberSaveable { mutableStateOf("") }
    var selectedNeed by remember { mutableStateOf(needOptions[0]) }
    var selectedIntensity by remember { mutableStateOf(intensityOptions[0]) }
    var notifSkipped by remember { mutableStateOf(false) }
    var notifRequested by remember { mutableStateOf(false) }

    val suggestions = OnboardingSuggestions.suggestedRoutines(selectedNeed.id, selectedIntensity.id)
        .map { it to stringResource(it.nameRes) }
    var selectedRoutineNames by remember(selectedNeed, selectedIntensity) {
        mutableStateOf(suggestions.filter { it.first.defaultSelected }.map { it.second }.toSet())
    }
    val taskTitle = stringResource(OnboardingSuggestions.suggestedTaskTitle(selectedNeed.id))
    var taskSelected by remember(selectedNeed) { mutableStateOf(true) }

    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { notifRequested = true }
    val notifGranted = notifRequested.let { _ ->
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    val finish = {
        onComplete(selectedNeed.id, selectedIntensity.id, selectedRoutineNames.toList(), if (taskSelected) taskTitle else null, userName)
    }
    BackHandler(enabled = step > 0) { step-- }

    val minoLine = when (step) {
        1 -> stringResource(R.string.onboarding_mino_1)
        2 -> stringResource(R.string.onboarding_mino_2)
        3 -> stringResource(R.string.onboarding_mino_3)
        4 -> stringResource(R.string.onboarding_mino_4)
        else -> null
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).imePadding()) {
        OnboardingSky(step = step, totalSteps = StepCount, minoLine = minoLine, onSkip = if (step < StepCount - 1) finish else null)

        AnimatedContent(
            targetState = step,
            transitionSpec = { fadeIn(tween(AppTokens.Motion.normal)) togetherWith fadeOut(tween(AppTokens.Motion.fast)) },
            label = "onboarding_step",
            modifier = Modifier.weight(1f),
        ) { current ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppTokens.Spacing.xl, vertical = AppTokens.Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm),
            ) {
                when (current) {
                    0 -> StepWelcome(userName = userName, onNameChange = { userName = it })
                    1 -> StepNeed(needOptions, selectedNeed) { selectedNeed = it }
                    2 -> StepTempo(intensityOptions, selectedIntensity) { selectedIntensity = it }
                    3 -> StepFirstSteps(
                        suggestions = suggestions,
                        selectedNames = selectedRoutineNames,
                        onToggleRoutine = { name ->
                            selectedRoutineNames = if (name in selectedRoutineNames) selectedRoutineNames - name else selectedRoutineNames + name
                        },
                        taskTitle = taskTitle,
                        taskSelected = taskSelected,
                        onToggleTask = { taskSelected = !taskSelected },
                    )
                    else -> StepReady(
                        userName = userName,
                        routineCount = selectedRoutineNames.size,
                        taskCount = if (taskSelected) 1 else 0,
                        notifGranted = notifGranted,
                        notifSkipped = notifSkipped,
                        onGrant = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        onSkipNotif = { notifSkipped = true },
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = AppTokens.Spacing.xl, vertical = AppTokens.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm),
        ) {
            if (step > 0) {
                TextButton(onClick = { step-- }, modifier = Modifier.heightIn(min = AppTokens.TouchTarget.min)) {
                    Text(
                        stringResource(R.string.action_back),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            SheetSaveButton(
                text = stringResource(if (step < StepCount - 1) R.string.action_next else R.string.onboarding_finish_btn),
                onClick = { if (step < StepCount - 1) step++ else finish() },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
