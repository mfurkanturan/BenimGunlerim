package com.benimgunlerim.domain.update

/**
 * Firebase Remote Config'ten gelen sürüm bilgisi.
 *
 * - [latestVersionCode]: mağazadaki en yeni sürüm. Bundan eskiyse "Güncelleme var" çıkar (kapatılabilir).
 * - [minVersionCode]: desteklenen en eski sürüm. Bundan eskiyse güncelleme zorunludur (kapatılamaz).
 *   0 bırakılırsa hiçbir sürüm zorlanmaz.
 */
data class UpdateConfig(
    val latestVersionCode: Long,
    val minVersionCode: Long,
    val message: String,
    val storeUrl: String,
)

sealed interface UpdatePrompt {
    data object None : UpdatePrompt
    data class Optional(val latestVersionCode: Long, val message: String, val storeUrl: String) : UpdatePrompt
    data class Required(val message: String, val storeUrl: String) : UpdatePrompt
}

/** Açılışta pop-up gösterilip gösterilmeyeceğine karar verir — saf, test edilir. */
object UpdatePolicy {
    /** "Sonra" denince aynı sürüm bir gün boyunca tekrar sorulmaz; ertesi açılışta yine hatırlatılır. */
    const val SNOOZE_MILLIS = 24L * 60 * 60 * 1000

    fun decide(
        currentVersionCode: Long,
        config: UpdateConfig?,
        snoozedVersionCode: Long,
        snoozedAtMillis: Long,
        nowMillis: Long,
    ): UpdatePrompt {
        if (config == null) return UpdatePrompt.None
        if (currentVersionCode < config.minVersionCode) {
            return UpdatePrompt.Required(config.message, config.storeUrl)
        }
        if (config.latestVersionCode <= currentVersionCode) return UpdatePrompt.None
        val snoozed = snoozedVersionCode == config.latestVersionCode && nowMillis - snoozedAtMillis in 0 until SNOOZE_MILLIS
        return if (snoozed) {
            UpdatePrompt.None
        } else {
            UpdatePrompt.Optional(config.latestVersionCode, config.message, config.storeUrl)
        }
    }
}
