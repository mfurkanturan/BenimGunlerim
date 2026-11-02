package com.benimgunlerim.domain.account

import com.benimgunlerim.data.DataExportService
import com.benimgunlerim.data.DataImportService
import com.benimgunlerim.data.LocalDataClearer
import com.benimgunlerim.data.UserPreferencesSource
import com.benimgunlerim.data.account.AccountError
import com.benimgunlerim.data.account.AccountPrefs
import com.benimgunlerim.data.account.AccountRepository
import com.benimgunlerim.data.account.AccountResult
import com.benimgunlerim.data.account.AccountState
import com.benimgunlerim.data.account.CloudBackupMeta
import com.benimgunlerim.data.account.CloudBackupStore
import com.benimgunlerim.data.account.EntryChoice
import com.benimgunlerim.data.local.RoutineDao
import com.benimgunlerim.data.local.TaskDao
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.notifications.NotificationPolicyCache
import com.benimgunlerim.notifications.ReminderRestorer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SyncOutcome {
    data class Done(val action: SyncAction) : SyncOutcome
    /** İlk eşitlemede hem telefonda hem bulutta veri var — kullanıcı seçecek. */
    data class NeedsChoice(val cloud: CloudBackupMeta, val localTasks: Int, val localRoutines: Int) : SyncOutcome
    data class Failed(val error: AccountError) : SyncOutcome
    data object NotSignedIn : SyncOutcome
}

enum class SyncStatus { Idle, Syncing, Error }

/**
 * Misafir ↔ hesap geçişi ve bulut yedeği. Telefon her zaman çalışan kopyadır (çevrimdışı tam çalışır);
 * bulut, uygulamanın dışa aktarma anlık görüntüsünün yedeğidir. Kararları [SyncPlanner] verir.
 */
@Singleton
class CloudSyncCoordinator @Inject constructor(
    private val account: AccountRepository,
    private val store: CloudBackupStore,
    private val prefs: AccountPrefs,
    private val exporter: DataExportService,
    private val importer: DataImportService,
    private val clearer: LocalDataClearer,
    private val taskDao: TaskDao,
    private val routineDao: RoutineDao,
    private val userPrefs: UserPreferencesSource,
    private val reminders: ReminderRestorer,
    private val policyCache: NotificationPolicyCache,
    private val clock: DateTimeProvider,
) {
    private val mutex = Mutex()
    private val _status = MutableStateFlow(SyncStatus.Idle)
    val status: StateFlow<SyncStatus> = _status.asStateFlow()

    suspend fun sync(trigger: SyncTrigger): SyncOutcome = mutex.withLock {
        val user = account.currentState() as? AccountState.SignedIn ?: return SyncOutcome.NotSignedIn
        _status.value = SyncStatus.Syncing
        val outcome = runCatching {
            val cloud = store.meta(user.uid)
            val tasks = taskDao.count()
            val routines = routineDao.count()
            val action = SyncPlanner.plan(
                trigger = trigger,
                firstSyncOnDevice = !prefs.hasSyncedAccount(user.uid),
                localTaskCount = tasks,
                localRoutineCount = routines,
                cloud = cloud,
                lastSyncAt = prefs.lastSyncAt.value,
            )
            when (action) {
                SyncAction.Upload -> upload(user.uid)
                SyncAction.Download -> download(user.uid)
                SyncAction.AskUser -> return@runCatching SyncOutcome.NeedsChoice(requireNotNull(cloud), tasks, routines)
                SyncAction.None -> Unit
            }
            SyncOutcome.Done(action)
        }.getOrElse { SyncOutcome.Failed(AccountError.Network) }
        _status.value = if (outcome is SyncOutcome.Failed) SyncStatus.Error else SyncStatus.Idle
        outcome
    }

    /** İlk eşitleme çakışmasında kullanıcının seçimi: buluttakini mi, telefondakini mi tut. */
    suspend fun resolveFirstSync(keepCloud: Boolean): SyncOutcome = mutex.withLock {
        val user = account.currentState() as? AccountState.SignedIn ?: return SyncOutcome.NotSignedIn
        runCatching {
            if (keepCloud) download(user.uid) else upload(user.uid)
            SyncOutcome.Done(if (keepCloud) SyncAction.Download else SyncAction.Upload)
        }.getOrElse { SyncOutcome.Failed(AccountError.Network) }
    }

    private suspend fun upload(uid: String) {
        val json = exporter.exportToJson() ?: error("export failed")
        val now = clock.currentTimeMillis()
        store.upload(uid, json, CloudBackupMeta(now, taskDao.count(), routineDao.count()))
        markSynced(uid, now)
    }

    private suspend fun download(uid: String) {
        val json = store.download(uid) ?: return
        importer.importFromJson(json) ?: error("import failed")
        val p = userPrefs.preferences.first()
        policyCache.updateCache(p.notificationMode, p.quietHoursEnabled, p.quietHoursStart, p.quietHoursEnd)
        reminders.rescheduleReminders()
        markSynced(uid, store.meta(uid)?.updatedAtMillis ?: clock.currentTimeMillis())
    }

    private fun markSynced(uid: String, at: Long) {
        prefs.markSyncedAccount(uid)
        prefs.setLastSyncAt(at)
        prefs.setEntryChoice(EntryChoice.Account)
    }

    /**
     * Çıkış: hesap kapanır, cihaz misafire döner. [clearDevice] ise telefondaki veri de silinir
     * (ortak telefonda başkası kullanacaksa). Önce son bir yedek alınır.
     */
    suspend fun signOut(clearDevice: Boolean) {
        sync(SyncTrigger.Manual)
        account.signOut()
        prefs.markSyncedAccount(null)
        prefs.setLastSyncAt(0L)
        prefs.setEntryChoice(EntryChoice.Guest)
        if (clearDevice) clearer.clearAllLocalData()
    }

    /** Hesabı ve buluttaki tüm veriyi kalıcı siler. Telefondaki veri misafir olarak kalır. */
    suspend fun deleteAccount(password: String): AccountResult<Unit> = mutex.withLock {
        val user = account.currentState() as? AccountState.SignedIn
            ?: return AccountResult.Failure(AccountError.UserNotFound)
        when (val re = account.reauthenticate(password)) {
            is AccountResult.Failure -> return re
            is AccountResult.Success -> Unit
        }
        runCatching { store.delete(user.uid) }.onFailure { return AccountResult.Failure(AccountError.Network) }
        val deleted = account.deleteUser()
        if (deleted is AccountResult.Success) {
            prefs.markSyncedAccount(null)
            prefs.setLastSyncAt(0L)
            prefs.setEntryChoice(EntryChoice.Guest)
        }
        deleted
    }
}
