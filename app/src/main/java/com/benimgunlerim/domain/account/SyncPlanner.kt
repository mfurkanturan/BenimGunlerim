package com.benimgunlerim.domain.account

import com.benimgunlerim.data.account.CloudBackupMeta

enum class SyncTrigger {
    /** Uygulama açılışı / hesaba yeni giriş. */
    Start,
    /** Uygulama arka plana geçti — sessiz yedek. */
    Background,
    /** Kullanıcı "Şimdi eşitle" dedi. */
    Manual,
}

enum class SyncAction { Upload, Download, AskUser, None }

/**
 * Eşitleme kararı — saf fonksiyon, test edilir.
 *
 * Kurallar:
 * - Bu cihazda bu hesapla ilk eşitleme: bulut boşsa telefon yüklenir; telefon boşsa bulut indirilir;
 *   ikisinde de veri varsa kullanıcıya sorulur (hiçbir veri sessizce ezilmez).
 * - Sonraki eşitlemeler: bulut bu cihazın son eşitlemesinden daha yeniyse (başka cihaz yazmış)
 *   açılışta ve elle eşitlemede indirilir; arka planda yazılmaz (yeni bulutu ezmemek için).
 *   Aksi halde arka planda ve elle eşitlemede yüklenir.
 */
object SyncPlanner {
    fun plan(
        trigger: SyncTrigger,
        firstSyncOnDevice: Boolean,
        localTaskCount: Int,
        localRoutineCount: Int,
        cloud: CloudBackupMeta?,
        lastSyncAt: Long,
    ): SyncAction {
        val localEmpty = localTaskCount == 0 && localRoutineCount == 0
        if (firstSyncOnDevice) {
            return when {
                cloud == null -> SyncAction.Upload
                localEmpty -> SyncAction.Download
                else -> SyncAction.AskUser
            }
        }
        if (cloud == null) return SyncAction.Upload
        val cloudIsNewer = cloud.updatedAtMillis > lastSyncAt
        return when (trigger) {
            SyncTrigger.Start -> if (cloudIsNewer) SyncAction.Download else SyncAction.None
            SyncTrigger.Background -> if (cloudIsNewer) SyncAction.None else SyncAction.Upload
            SyncTrigger.Manual -> if (cloudIsNewer) SyncAction.Download else SyncAction.Upload
        }
    }
}
