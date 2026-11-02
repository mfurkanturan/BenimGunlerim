package com.benimgunlerim.domain.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdatePolicyTest {

    private val now = 10L * UpdatePolicy.SNOOZE_MILLIS

    private fun config(latest: Long, min: Long = 0L) = UpdateConfig(latest, min, "msg", "url")

    private fun decide(current: Long, config: UpdateConfig?, snoozedVersion: Long = 0L, snoozedAt: Long = 0L) =
        UpdatePolicy.decide(current, config, snoozedVersion, snoozedAt, now)

    @Test fun `config yoksa pop-up yok`() =
        assertEquals(UpdatePrompt.None, decide(current = 4, config = null))

    @Test fun `en guncel surumdeyse pop-up yok`() =
        assertEquals(UpdatePrompt.None, decide(current = 5, config = config(latest = 5)))

    @Test fun `latest 0 iken hicbir sey gosterilmez`() =
        assertEquals(UpdatePrompt.None, decide(current = 4, config = config(latest = 0)))

    @Test fun `yeni surum varsa istege bagli pop-up`() =
        assertEquals(UpdatePrompt.Optional(5, "msg", "url"), decide(current = 4, config = config(latest = 5)))

    @Test fun `min surumun altindaysa zorunlu`() =
        assertTrue(decide(current = 3, config = config(latest = 5, min = 4)) is UpdatePrompt.Required)

    @Test fun `zorunlu guncelleme ertelemeyi yok sayar`() =
        assertTrue(decide(current = 3, config = config(latest = 5, min = 4), snoozedVersion = 5, snoozedAt = now) is UpdatePrompt.Required)

    @Test fun `sonra denen surum bir gun boyunca sorulmaz`() =
        assertEquals(UpdatePrompt.None, decide(current = 4, config = config(latest = 5), snoozedVersion = 5, snoozedAt = now - 1000))

    @Test fun `bir gun sonra yine hatirlatilir`() =
        assertTrue(
            decide(current = 4, config = config(latest = 5), snoozedVersion = 5, snoozedAt = now - UpdatePolicy.SNOOZE_MILLIS) is UpdatePrompt.Optional,
        )

    @Test fun `ertelenenden daha yeni bir surum cikarsa hemen sorulur`() =
        assertTrue(decide(current = 4, config = config(latest = 6), snoozedVersion = 5, snoozedAt = now) is UpdatePrompt.Optional)
}
