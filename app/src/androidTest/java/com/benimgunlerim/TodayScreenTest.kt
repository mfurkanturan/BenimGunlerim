package com.benimgunlerim

import android.content.Intent
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.benimgunlerim.ui.TestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TodayScreenTest {

    private val intent = Intent(
        InstrumentationRegistry.getInstrumentation().targetContext,
        MainActivity::class.java,
    ).putExtra("force_onboarding_completed", true)

    @get:Rule
    val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity> =
        AndroidComposeTestRule(
            activityRule = ActivityScenarioRule(intent),
        ) { rule ->
            var activity: MainActivity? = null
            rule.scenario.onActivity { activity = it }
            activity!!
        }

    @Test
    fun today_screen_root_is_visible() {
        composeTestRule
            .onNodeWithTag(TestTags.TodayRoot)
            .assertIsDisplayed()
    }

    @Test
    fun today_bottom_nav_item_is_visible() {
        composeTestRule
            .onNodeWithTag(TestTags.BottomNavToday)
            .assertIsDisplayed()
    }

    @Test
    fun today_fab_is_visible() {
        scrollTodayTo(TestTags.TodayFab)
        composeTestRule
            .onNodeWithTag(TestTags.TodayFab)
            .assertIsDisplayed()
    }

    @Test
    fun today_close_day_card_is_visible() {
        scrollTodayTo(TestTags.TodayCloseDayCard)
        composeTestRule
            .onNodeWithTag(TestTags.TodayCloseDayCard)
            .assertIsDisplayed()
    }

    @Test
    fun today_snapshot_error_banner_hidden_when_load_ok() {
        composeTestRule.onAllNodesWithTag(TestTags.TodaySnapshotErrorBanner).assertCountEquals(0)
    }

    @Test
    fun routines_tab_is_accessible_via_nav_tag() {
        composeTestRule
            .onNodeWithTag(TestTags.BottomNavRoutines)
            .performClick()
        composeTestRule
            .onNodeWithTag(TestTags.RoutinesRoot)
            .assertIsDisplayed()
    }

    @Test
    fun deleting_task_shows_confirmation_dialog() {
        // Her koşuda benzersiz ad: önceki yarım kalmış koşulardan kalan görevlerle karışmasın.
        val title = "Test gorevi ${System.currentTimeMillis() % 100_000}"

        scrollTodayTo(TestTags.TodayFab)
        // Dolu günde TodayFab hızlı menüyü açar; boş günde doğrudan görev formunu açar.
        composeTestRule.onNodeWithTag(TestTags.TodayFab).performClick()
        val menuTitle = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.quick_action_add_task_title)
        if (composeTestRule.onAllNodesWithText(menuTitle).fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithText(menuTitle).performClick()
        }
        composeTestRule.onNodeWithTag(TestTags.AddTaskTitleField).performTextInput(title)
        composeTestRule.onNodeWithText("Kaydet").performClick()
        // Yeni görev listede aşağıda kalabilir (ör. "Bugün biraz yoğun mu?" kartı üstte belirir); ona kaydır.
        composeTestRule.waitUntil(5_000) {
            runCatching {
                composeTestRule.onNodeWithTag(TestTags.TodayList).performScrollToNode(hasText(title))
            }.isSuccess
        }

        // Bugün'de silme, göreve dokununca açılan ayrıntı sayfasından yapılır.
        val deleteLabel = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.today_delete_label)
        composeTestRule.onNodeWithText(title).performClick()
        composeTestRule.onNodeWithText(deleteLabel).performScrollTo().performClick()
        composeTestRule.onNodeWithText(
            InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.today_delete_task_title),
        ).assertIsDisplayed()

        // Temizlik: testin eklediği görevi gerçekten sil — kullanıcının listesinde iz kalmasın.
        composeTestRule.onAllNodesWithText(deleteLabel).onLast().performClick()
    }

    /** Bugün listesi uzun; alttaki öğeleri doğrulamadan önce listeyi o öğeye kaydır. */
    private fun scrollTodayTo(tag: String) {
        composeTestRule.onNodeWithTag(TestTags.TodayList).performScrollToNode(hasTestTag(tag))
    }
}
