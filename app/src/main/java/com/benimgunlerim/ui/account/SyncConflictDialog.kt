package com.benimgunlerim.ui.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.DialogProperties
import com.benimgunlerim.R
import com.benimgunlerim.domain.account.SyncOutcome
import com.benimgunlerim.ui.theme.AppTokens
import java.text.DateFormat
import java.util.Date

/** Hem telefonda hem hesapta veri varken: hangisi tutulsun? Hiçbir veri sessizce ezilmez. */
@Composable
fun SyncConflictDialog(conflict: SyncOutcome.NeedsChoice, onKeepCloud: () -> Unit, onKeepPhone: () -> Unit) {
    val date = remember(conflict) { DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(conflict.cloud.updatedAtMillis)) }
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = { Text(stringResource(R.string.sync_conflict_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm)) {
                Text(stringResource(R.string.sync_conflict_body))
                ChoiceOption(
                    title = stringResource(R.string.sync_conflict_cloud),
                    meta = stringResource(R.string.sync_conflict_cloud_meta, conflict.cloud.taskCount, conflict.cloud.routineCount, date),
                    onClick = onKeepCloud,
                )
                ChoiceOption(
                    title = stringResource(R.string.sync_conflict_phone),
                    meta = stringResource(R.string.sync_conflict_phone_meta, conflict.localTasks, conflict.localRoutines),
                    onClick = onKeepPhone,
                )
            }
        },
        confirmButton = {},
    )
}

@Composable
private fun ChoiceOption(title: String, meta: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(AppTokens.Radius.lg),
        border = BorderStroke(AppTokens.BorderWidth.thin, MaterialTheme.colorScheme.outline),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().heightIn(min = AppTokens.Layout.fabDefaultSize),
    ) {
        Column(modifier = Modifier.padding(AppTokens.Spacing.md)) {
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
            Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
