package com.benimgunlerim.ui.routines

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.core.AppButton
import com.benimgunlerim.ui.components.core.AppButtonVariant
import com.benimgunlerim.ui.components.core.AppDivider
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.components.molecules.SectionBlock
import com.benimgunlerim.ui.theme.AppTokens

@Composable
internal fun ArchivedRoutinesSection(
    archivedRoutines: List<ArchivedRoutineUi>,
    onUnarchive: (String) -> Unit,
) {
    SectionBlock(title = stringResource(R.string.routines_archived_section_title)) {
        AppSurface(radius = AppTokens.Radius.lg, padding = AppTokens.Spacing.none) {
            Column {
                archivedRoutines.forEachIndexed { index, archived ->
                    key(archived.id) {
                        if (index > 0) AppDivider()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppTokens.Spacing.cardInner),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = archived.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                            )
                            AppButton(
                                text = stringResource(R.string.routines_unarchive_action),
                                onClick = { onUnarchive(archived.id) },
                                variant = AppButtonVariant.Ghost,
                            )
                        }
                    }
                }
            }
        }
    }
}

internal fun Modifier.routineReorderGesture(
    routine: RoutineCardUi,
    routines: List<RoutineCardUi>,
    onReorder: (List<String>) -> Unit,
): Modifier = pointerInput(routine.id, routines.map { it.id }) {
    detectDragGesturesAfterLongPress { change, drag ->
        val from = routines.indexOfFirst { it.id == routine.id }
        val to = when {
            drag.y > 28f -> (from + 1).coerceAtMost(routines.lastIndex)
            drag.y < -28f -> (from - 1).coerceAtLeast(0)
            else -> from
        }
        if (from != to) {
            val reordered = routines.toMutableList().apply { add(to, removeAt(from)) }
            onReorder(reordered.map { it.id })
        }
    }
}
