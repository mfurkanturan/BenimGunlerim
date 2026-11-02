package com.benimgunlerim.data.update

import android.content.Context
import com.benimgunlerim.BuildConfig
import com.benimgunlerim.domain.update.UpdateConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Sürüm bilgisinin kaynağı; testte sahtesi verilir. */
interface UpdateConfigSource {
    /** Ağ yoksa ya da Firebase kurulu değilse null — o durumda pop-up çıkmaz. */
    suspend fun fetch(): UpdateConfig?
}

/**
 * Firebase Console › Remote Config anahtarları:
 * `latest_version_code`, `min_version_code`, `update_message`, `update_store_url`.
 * Yeni sürümü mağazaya yükleyince yalnızca `latest_version_code`'u artırmak yeter.
 */
@Singleton
class FirebaseUpdateConfigSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : UpdateConfigSource {

    override suspend fun fetch(): UpdateConfig? {
        if (FirebaseApp.getApps(context).isEmpty()) return null
        return runCatching {
            val rc = FirebaseRemoteConfig.getInstance()
            rc.setConfigSettingsAsync(
                FirebaseRemoteConfigSettings.Builder()
                    // Test sürümünde Console'daki değişiklik hemen görünsün; yayında saatte bir yeter.
                    .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else FETCH_INTERVAL_SECONDS)
                    .build(),
            ).await()
            rc.setDefaultsAsync(
                mapOf(
                    KEY_LATEST to 0L,
                    KEY_MIN to 0L,
                    KEY_MESSAGE to "",
                    KEY_STORE_URL to DEFAULT_STORE_URL,
                ),
            ).await()
            rc.fetchAndActivate().await()
            UpdateConfig(
                latestVersionCode = rc.getLong(KEY_LATEST),
                minVersionCode = rc.getLong(KEY_MIN),
                message = rc.getString(KEY_MESSAGE),
                storeUrl = rc.getString(KEY_STORE_URL).ifBlank { DEFAULT_STORE_URL },
            )
        }.getOrNull()
    }

    private companion object {
        const val KEY_LATEST = "latest_version_code"
        const val KEY_MIN = "min_version_code"
        const val KEY_MESSAGE = "update_message"
        const val KEY_STORE_URL = "update_store_url"
        const val FETCH_INTERVAL_SECONDS = 60L * 60
        const val DEFAULT_STORE_URL = "https://play.google.com/store/apps/details?id=com.benimgunlerim"
    }
}

/** "Sonra" seçiminin hatırlandığı yer. */
interface UpdateSnoozeStore {
    val snoozedVersionCode: Long
    val snoozedAtMillis: Long
    fun snooze(versionCode: Long, atMillis: Long)
}

@Singleton
class SharedUpdateSnoozeStore @Inject constructor(
    @ApplicationContext context: Context,
) : UpdateSnoozeStore {
    private val prefs = context.getSharedPreferences("update_prefs", Context.MODE_PRIVATE)

    override val snoozedVersionCode: Long get() = prefs.getLong(KEY_VERSION, 0L)
    override val snoozedAtMillis: Long get() = prefs.getLong(KEY_AT, 0L)

    override fun snooze(versionCode: Long, atMillis: Long) {
        prefs.edit().putLong(KEY_VERSION, versionCode).putLong(KEY_AT, atMillis).apply()
    }

    private companion object {
        const val KEY_VERSION = "snoozed_version_code"
        const val KEY_AT = "snoozed_at"
    }
}
