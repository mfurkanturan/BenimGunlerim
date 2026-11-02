package com.benimgunlerim

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.benimgunlerim.ui.components.organisms.SwipeToDelete
import com.benimgunlerim.ui.theme.BenimGunlerimTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SwipeToDeleteTest {
    @get:Rule val rule = createComposeRule()

    private var deletes = 0

    private fun setContent() = rule.setContent {
        BenimGunlerimTheme {
            SwipeToDelete(onDelete = { deletes++ }, modifier = Modifier.testTag("row")) {
                Text("Görev", modifier = Modifier.fillMaxWidth())
            }
        }
    }

    @Test fun sola_kaydirinca_siler() {
        setContent()
        rule.onNodeWithTag("row").performTouchInput { swipeLeft() }
        rule.waitForIdle()
        assertEquals(1, deletes)
    }

    @Test fun saga_kaydirinca_silmez() {
        setContent()
        rule.onNodeWithTag("row").performTouchInput { swipeRight() }
        rule.waitForIdle()
        assertEquals(0, deletes)
    }

    @Test fun ekran_okuyucu_eylemiyle_siler() {
        setContent()
        val actions = rule.onNodeWithTag("row").fetchSemanticsNode().config[SemanticsActions.CustomActions]
        rule.runOnIdle { actions.first().action() }
        assertEquals(1, deletes)
    }
}
