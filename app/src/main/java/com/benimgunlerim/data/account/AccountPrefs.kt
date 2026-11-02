package com.benimgunlerim.data.account

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hesapla ilgili küçük cihaz ayarları. Bilerek ayrı dosyada: yedekten geri yükleme (import)
 * kullanıcı tercihlerini değiştirir ama bu seçimleri ezmemeli.
 */
interface AccountPrefs {
    val entryChoice: StateFlow<EntryChoice>
    fun setEntryChoice(choice: EntryChoice)
    /** Son başarılı bulut eşitlemesi (ms); hiç yoksa 0. */
    val lastSyncAt: StateFlow<Long>
    fun setLastSyncAt(millis: Long)
    /** Bu cihazda bu hesapla ilk eşitleme yapıldı mı (ilk girişte birleştirme sorusu için). */
    fun hasSyncedAccount(uid: String): Boolean
    fun markSyncedAccount(uid: String?)
}

@Singleton
class SharedAccountPrefs @Inject constructor(
    @ApplicationContext context: Context,
) : AccountPrefs {
    private val sp = context.getSharedPreferences("account_prefs", Context.MODE_PRIVATE)

    private val _entryChoice = MutableStateFlow(
        runCatching { EntryChoice.valueOf(sp.getString(K_CHOICE, null) ?: "") }.getOrDefault(EntryChoice.Unset),
    )
    override val entryChoice: StateFlow<EntryChoice> = _entryChoice.asStateFlow()

    override fun setEntryChoice(choice: EntryChoice) {
        sp.edit().putString(K_CHOICE, choice.name).apply()
        _entryChoice.value = choice
    }

    private val _lastSyncAt = MutableStateFlow(sp.getLong(K_LAST_SYNC, 0L))
    override val lastSyncAt: StateFlow<Long> = _lastSyncAt.asStateFlow()

    override fun setLastSyncAt(millis: Long) {
        sp.edit().putLong(K_LAST_SYNC, millis).apply()
        _lastSyncAt.value = millis
    }

    override fun hasSyncedAccount(uid: String): Boolean = sp.getString(K_SYNCED_UID, null) == uid

    override fun markSyncedAccount(uid: String?) {
        sp.edit().putString(K_SYNCED_UID, uid).apply()
    }

    private companion object {
        const val K_CHOICE = "entry_choice"
        const val K_LAST_SYNC = "last_sync_at"
        const val K_SYNCED_UID = "synced_uid"
    }
}
