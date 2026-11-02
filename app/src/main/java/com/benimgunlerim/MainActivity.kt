package com.benimgunlerim

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.ui.theme.DayPhase
import com.benimgunlerim.ui.theme.DebugSkyPhase
import com.benimgunlerim.ui.onboarding.OnboardingViewModel
import com.benimgunlerim.ui.navigation.BenimGunlerimApp
import com.benimgunlerim.ui.theme.BenimGunlerimTheme
import com.benimgunlerim.notifications.NotificationConstants
import dagger.hilt.android.AndroidEntryPoint
import com.benimgunlerim.di.ApplicationScope
import com.benimgunlerim.domain.account.CloudSyncCoordinator
import com.benimgunlerim.domain.account.SyncTrigger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var cloudSync: CloudSyncCoordinator

    @Inject @ApplicationScope lateinit var appScope: CoroutineScope

    // Hesaptaysa: açılışta başka cihazın yeni yedeğini al; arka plana geçince sessizce yedekle.
    override fun onStart() {
        super.onStart()
        appScope.launch { cloudSync.sync(SyncTrigger.Start) }
    }

    override fun onStop() {
        super.onStop()
        appScope.launch { cloudSync.sync(SyncTrigger.Background) }
    }

    private companion object {
        const val EXTRA_FORCE_ONBOARDING_COMPLETED = "force_onboarding_completed"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge-to-edge kullanıldığı için XML'deki status bar ayarları tek başına
        // yeterli değildir. Açık modda sistem ikonlarını özellikle koyu tutuyoruz.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        if (BuildConfig.DEBUG) {
            DebugSkyPhase.override = intent.getStringExtra("debug_sky_phase")
                ?.let { name -> DayPhase.entries.firstOrNull { it.name == name } }
            DebugSkyPhase.forceEmptyToday = intent.getBooleanExtra("debug_empty_today", false)
            DebugSkyPhase.forceSamplePlan = intent.getBooleanExtra("debug_sample_plan", false)
            DebugSkyPhase.forceAccountUi = intent.getBooleanExtra("debug_account_ui", false)
            DebugSkyPhase.forceUpdatePrompt = intent.getStringExtra("debug_update_prompt")
        }
        setContent {
            val onboardingViewModel: OnboardingViewModel = hiltViewModel()
            val forceOnboardingCompleted = BuildConfig.DEBUG &&
                intent.getBooleanExtra(EXTRA_FORCE_ONBOARDING_COMPLETED, false)
            BenimGunlerimTheme {
                BenimGunlerimApp(
                    requestedStartRoute = intent.getStringExtra(NotificationConstants.EXTRA_START_ROUTE),
                    forceOnboardingCompleted = forceOnboardingCompleted,
                    viewModel = onboardingViewModel,
                )
            }
        }
    }
}
