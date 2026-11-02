package com.benimgunlerim.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.benimgunlerim.data.CompletionLogRepository
import com.benimgunlerim.domain.DateTimeProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RoutineReminderReceiver : BroadcastReceiver() {
    @Inject
    lateinit var dateTimeProvider: DateTimeProvider

    @Inject
    lateinit var completionLogRepository: CompletionLogRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != NotificationConstants.ACTION_ROUTINE_REMINDER) return
        val routineId = intent.getStringExtra(NotificationConstants.EXTRA_ROUTINE_ID) ?: return
        val routineName = intent.getStringExtra(NotificationConstants.EXTRA_ROUTINE_NAME) ?: "Rutinin"
        val targetDays = intent.getStringExtra(NotificationConstants.EXTRA_TARGET_DAYS).orEmpty()
        val today = dateTimeProvider.today()
        // Bugün zaten yapıldıysa hatırlatma yok — veritabanına arka planda bakılır.
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val todayLogs = runCatching { completionLogRepository.observeByDate(today).first() }.getOrDefault(emptyList())
                if (RoutineReminderGate.shouldRemind(routineId, targetDays, today.dayOfWeek, todayLogs)) {
                    context.showRoutineReminder(routineId, routineName)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
