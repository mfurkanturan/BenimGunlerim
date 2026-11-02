package com.benimgunlerim.ui.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benimgunlerim.BuildConfig
import com.benimgunlerim.data.update.UpdateConfigSource
import com.benimgunlerim.data.update.UpdateSnoozeStore
import com.benimgunlerim.domain.update.UpdateConfig
import com.benimgunlerim.domain.update.UpdatePolicy
import com.benimgunlerim.domain.update.UpdatePrompt
import com.benimgunlerim.ui.theme.DebugSkyPhase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Açılışta bir kez sürüm bilgisini çeker; yeni sürüm varsa pop-up durumunu yayınlar. */
@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val source: UpdateConfigSource,
    private val snoozeStore: UpdateSnoozeStore,
) : ViewModel() {

    private val _prompt = MutableStateFlow<UpdatePrompt>(UpdatePrompt.None)
    val prompt: StateFlow<UpdatePrompt> = _prompt.asStateFlow()

    /** Testte sabit bir sürüm verilebilsin diye ayrı alan. */
    internal var currentVersionCode: Long = BuildConfig.VERSION_CODE.toLong()
    internal var clock: () -> Long = System::currentTimeMillis

    init {
        viewModelScope.launch { check() }
    }

    internal suspend fun check() {
        _prompt.value = UpdatePolicy.decide(
            currentVersionCode = currentVersionCode,
            config = debugOverride(source.fetch()),
            snoozedVersionCode = snoozeStore.snoozedVersionCode,
            snoozedAtMillis = snoozeStore.snoozedAtMillis,
            nowMillis = clock(),
        )
    }

    /** Yalnızca debug: canlı Remote Config'i değiştirmeden pop-up'ı göstermek için sürümü bir öne alır. */
    private fun debugOverride(config: UpdateConfig?): UpdateConfig? {
        val forced = if (BuildConfig.DEBUG) DebugSkyPhase.forceUpdatePrompt else null
        val base = config ?: UpdateConfig(0L, 0L, "", "")
        return when (forced) {
            "optional" -> base.copy(latestVersionCode = currentVersionCode + 1)
            "required" -> base.copy(minVersionCode = currentVersionCode + 1)
            else -> config
        }
    }

    /** "Sonra": yalnızca isteğe bağlı güncellemede; zorunluda pop-up kapanmaz. */
    fun later() {
        val p = _prompt.value as? UpdatePrompt.Optional ?: return
        snoozeStore.snooze(p.latestVersionCode, clock())
        _prompt.value = UpdatePrompt.None
    }
}
