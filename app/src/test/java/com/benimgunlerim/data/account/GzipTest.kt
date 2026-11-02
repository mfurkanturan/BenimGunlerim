package com.benimgunlerim.data.account

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GzipTest {
    @Test fun roundTripKeepsTurkishTextAndShrinks() {
        val json = """{"tasks":[""" + (1..200).joinToString(",") { """{"title":"Güne başla, şöyle ılık su iç $it"}""" } + "]}"
        val packed = gzip(json)
        assertEquals(json, gunzip(packed))
        assertTrue(packed.size < json.toByteArray().size / 3)
    }
}
