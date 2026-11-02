package com.benimgunlerim.ui.today

import com.benimgunlerim.ui.theme.AppTheme
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import com.benimgunlerim.ui.theme.NightSurfaceTheme

/** Günü kapat: gece sahnesi. Sayfa ve içi yerel gece renk şemasıyla çizilir. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CloseSheetModal(
    completedCount: Int,
    totalCount: Int,
    overdueCount: Int,
    onDismiss: () -> Unit,
    onSave: (CloseDayDraft) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.colors.night.sky,
        contentColor = AppTheme.colors.night.text,
        dragHandle = { BottomSheetDefaults.DragHandle(color = AppTheme.colors.night.muted) },
    ) {
        NightSurfaceTheme {
            CloseDaySheet(
                completedCount = completedCount,
                totalCount = totalCount,
                progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f,
                overdueCount = overdueCount,
                onSave = onSave,
            )
        }
    }
}
