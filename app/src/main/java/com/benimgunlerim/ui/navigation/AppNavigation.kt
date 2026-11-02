@file:Suppress("SpellCheckingInspection")
package com.benimgunlerim.ui.navigation

import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.benimgunlerim.ui.theme.AppTheme
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.benimgunlerim.R
import com.benimgunlerim.ui.AppDestination
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.achievements.AchievementsScreen
import com.benimgunlerim.ui.onboarding.OnboardingScreen
import com.benimgunlerim.ui.onboarding.OnboardingViewModel
import com.benimgunlerim.ui.plan.PlanScreen
import com.benimgunlerim.ui.progress.ProgressScreen
import com.benimgunlerim.ui.routines.RoutineDetailScreen
import com.benimgunlerim.ui.routines.RoutinesScreen
import com.benimgunlerim.ui.settings.SettingsScreen
import com.benimgunlerim.ui.settings.PrivacyPolicyScreen
import com.benimgunlerim.ui.account.AccountGateScreen
import com.benimgunlerim.ui.account.AccountSection
import com.benimgunlerim.ui.account.AccountViewModel
import com.benimgunlerim.ui.account.AuthMode
import com.benimgunlerim.ui.account.AuthScreen
import com.benimgunlerim.data.account.EntryChoice
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.benimgunlerim.ui.shop.ShopScreen
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.today.TodayRoute
import com.benimgunlerim.domain.update.UpdatePrompt
import com.benimgunlerim.ui.update.UpdateDialog
import com.benimgunlerim.ui.update.UpdateViewModel

enum class Destination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    Today(
        route = AppDestination.Today.route,
        labelRes = R.string.nav_today,
        icon = Icons.Outlined.WbSunny,
        selectedIcon = Icons.Rounded.WbSunny,
    ),
    Plan(
        route = AppDestination.Plan.route,
        labelRes = R.string.nav_plan,
        icon = Icons.Outlined.CalendarMonth,
        selectedIcon = Icons.Rounded.CalendarMonth,
    ),
    Routines(
        route = AppDestination.Routines.route,
        labelRes = R.string.nav_routines,
        icon = Icons.Outlined.Repeat,
        selectedIcon = Icons.Rounded.Repeat,
    ),
    Progress(
        route = AppDestination.Progress.route,
        labelRes = R.string.nav_progress,
        icon = Icons.Outlined.StarOutline,
        selectedIcon = Icons.Rounded.Star,
    ),
    Settings(
        route = AppDestination.Settings.route,
        labelRes = R.string.nav_settings,
        icon = Icons.Outlined.Tune,
        selectedIcon = Icons.Rounded.Tune,
    ),
}

@Composable
fun BenimGunlerimApp(
    requestedStartRoute: String? = null,
    forceOnboardingCompleted: Boolean = false,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val accountViewModel: AccountViewModel = hiltViewModel()
    val entryChoice by accountViewModel.entryChoice.collectAsState()
    val userPreferences by viewModel.preferences.collectAsState()
    val effectiveCompleted = forceOnboardingCompleted || userPreferences.onboardingCompleted

    val startDestination = when {
        // İlk açılış (veya güncellemeden sonra ilk kez): Hesap aç / Giriş yap / Misafir.
        accountViewModel.isAvailable && entryChoice == EntryChoice.Unset && !forceOnboardingCompleted -> AppDestination.AccountGate.route
        !effectiveCompleted -> AppDestination.Onboarding.route
        requestedStartRoute == AppDestination.Plan.route -> AppDestination.Plan.route
        requestedStartRoute == AppDestination.Routines.route -> AppDestination.Routines.route
        else -> AppDestination.Today.route
    }

    val showBottomBar = currentRoute in Destination.entries.map { it.route }

    LaunchedEffect(requestedStartRoute, effectiveCompleted) {
        if (effectiveCompleted && requestedStartRoute != null) {
            val target = when (requestedStartRoute) {
                AppDestination.Plan.route -> AppDestination.Plan.route
                AppDestination.Routines.route -> AppDestination.Routines.route
                else -> null
            }
            if (target != null && currentRoute != target) {
                navController.navigate(target) {
                    popUpTo(Destination.Today.route) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    // Üst çubuk yok: her sekme kendi başlığını çizer (Bugün göğü durum çubuğunun altına uzanır).
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            if (showBottomBar) {
                val lineColor = MaterialTheme.colorScheme.outlineVariant
                NavigationBar(
                    modifier = Modifier.drawBehind {
                        drawLine(lineColor, Offset(0f, 0f), Offset(size.width, 0f), AppTokens.BorderWidth.thin.toPx())
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = AppTokens.Elevation.flat,
                ) {
                    Destination.entries.forEach { destination ->
                        val selected = currentRoute == destination.route
                        NavigationBarItem(
                            selected = selected,
                            modifier = Modifier.testTag(
                                when (destination) {
                                    Destination.Today    -> TestTags.BottomNavToday
                                    Destination.Plan     -> TestTags.BottomNavPlan
                                    Destination.Routines -> TestTags.BottomNavRoutines
                                    Destination.Progress -> TestTags.BottomNavProgress
                                    Destination.Settings -> TestTags.BottomNavSettings
                                },
                            ),
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(Destination.Today.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            label = {
                                Text(
                                    stringResource(destination.labelRes),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    ),
                                )
                            },
                            icon = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(AppTokens.BottomNav.indicatorDot)
                                            .background(if (selected) AppTheme.colors.progress.done else Color.Transparent, CircleShape),
                                    )
                                    Icon(
                                        imageVector = if (selected) destination.selectedIcon else destination.icon,
                                        contentDescription = stringResource(destination.labelRes),
                                    )
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .padding(padding)
                .then(
                    // Bugün ve onboarding gökyüzü durum çubuğunun altına uzanır; diğerleri boşluk bırakır
                    if (currentRoute == Destination.Today.route || currentRoute == AppDestination.Onboarding.route || currentRoute == AppDestination.AccountGate.route) Modifier
                    else Modifier.statusBarsPadding(),
                ),
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(200)) },
            popExitTransition = { fadeOut(tween(200)) },
        ) {
            // Girişten sonra: onboarding bitmişse (ör. buluttan geri geldi) Bugün, yoksa onboarding.
            val afterEntry: () -> Unit = {
                val target = if (viewModel.preferences.value.onboardingCompleted) Destination.Today.route else AppDestination.Onboarding.route
                navController.navigate(target) { popUpTo(navController.graph.id) { inclusive = true } }
            }
            composable(AppDestination.AccountGate.route) {
                AccountGateScreen(
                    onRegister = { navController.navigate(AppDestination.Auth.createRoute(AuthMode.Register)) },
                    onLogin = { navController.navigate(AppDestination.Auth.createRoute(AuthMode.Login)) },
                    onGuest = {
                        accountViewModel.continueAsGuest()
                        afterEntry()
                    },
                )
            }
            composable(AppDestination.Auth.route) { entry ->
                val mode = runCatching { AuthMode.valueOf(entry.arguments?.getString(AppDestination.Auth.ARG_MODE).orEmpty()) }
                    .getOrDefault(AuthMode.Login)
                var showPrivacy by remember { mutableStateOf(false) }
                AuthScreen(
                    initialMode = mode,
                    viewModel = accountViewModel,
                    onBack = { navController.popBackStack() },
                    onFinished = {
                        accountViewModel.clearMessages()
                        if (navController.previousBackStackEntry?.destination?.route == Destination.Settings.route) {
                            navController.popBackStack()
                        } else {
                            afterEntry()
                        }
                    },
                    onOpenPrivacy = { showPrivacy = true },
                )
                if (showPrivacy) {
                    Dialog(onDismissRequest = { showPrivacy = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                        PrivacyPolicyScreen(onNavigateBack = { showPrivacy = false })
                    }
                }
            }
            composable(AppDestination.Onboarding.route) {
                OnboardingScreen(
                    onComplete = { needId, intensityId, routineNames, taskTitle, userName ->
                        viewModel.completeOnboarding(needId, intensityId, routineNames, taskTitle, userName)
                        navController.navigate(Destination.Today.route) {
                            popUpTo(AppDestination.Onboarding.route) { inclusive = true }
                        }
                    },
                )
            }
            composable(Destination.Today.route) {
                TodayRoute(
                    onNavigateToRoutines = {
                        navController.navigate(Destination.Routines.route) {
                            popUpTo(Destination.Today.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToPlan = {
                        navController.navigate(Destination.Plan.route) {
                            popUpTo(Destination.Today.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate(Destination.Settings.route) {
                            popUpTo(Destination.Today.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenRoutineDetail = { routineId ->
                        navController.navigate(AppDestination.RoutineDetailPattern.createRoute(routineId))
                    },
                )
            }
            composable(Destination.Plan.route) { PlanScreen() }
            composable(Destination.Routines.route) {
                RoutinesScreen(onOpenRoutineDetail = { routineId ->
                    navController.navigate(AppDestination.RoutineDetailPattern.createRoute(routineId))
                })
            }
            composable(AppDestination.RoutineDetailPattern.route) { backStackEntry ->
                val routineId = backStackEntry.arguments?.getString(
                    AppDestination.RoutineDetailPattern.ARG_ROUTINE_ID,
                ) ?: return@composable
                RoutineDetailScreen(
                    routineId = routineId,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Destination.Progress.route) {
                ProgressScreen(
                    onNavigateToAchievements = { navController.navigate(AppDestination.Achievements.route) },
                )
            }
            composable(AppDestination.Achievements.route) {
                AchievementsScreen(onBack = { navController.popBackStack() })
            }
            composable(AppDestination.Shop.route) {
                ShopScreen(onBack = { navController.popBackStack() })
            }
            composable(Destination.Settings.route) {
                SettingsScreen(
                    onNavigateToOnboarding = {
                        navController.navigate(AppDestination.Onboarding.route)
                    },
                    accountSection = {
                        AccountSection(
                            viewModel = accountViewModel,
                            onRegister = { navController.navigate(AppDestination.Auth.createRoute(AuthMode.Register)) },
                            onLogin = { navController.navigate(AppDestination.Auth.createRoute(AuthMode.Login)) },
                        )
                    },
                )
            }
        }
    }

    // Yeni sürüm: isteğe bağlıysa ana sekmelerde (açılış/kayıt akışını bölmeden), zorunluysa her yerde.
    val updateViewModel: UpdateViewModel = hiltViewModel()
    val updatePrompt by updateViewModel.prompt.collectAsState()
    if (updatePrompt is UpdatePrompt.Required || (updatePrompt is UpdatePrompt.Optional && showBottomBar)) {
        UpdateDialog(prompt = updatePrompt, onLater = updateViewModel::later)
    }
}
