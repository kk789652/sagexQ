package com.tiswu.sagex.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AutostartIntentsTest {

    @Test
    fun `xiaomi maps to miui security center`() {
        val candidates = AutostartIntents.candidatesFor("Xiaomi")
        assertTrue(candidates.isNotEmpty())
        assertTrue(candidates.any { it.packageName == "com.miui.securitycenter" })
    }

    @Test
    fun `redmi and poco alias to xiaomi candidates`() {
        assertEquals(
            AutostartIntents.candidatesFor("Xiaomi"),
            AutostartIntents.candidatesFor("Redmi"),
        )
        assertEquals(
            AutostartIntents.candidatesFor("Xiaomi"),
            AutostartIntents.candidatesFor("POCO"),
        )
    }

    @Test
    fun `manufacturer match is case insensitive`() {
        assertTrue(AutostartIntents.candidatesFor("HUAWEI").isNotEmpty())
        assertTrue(AutostartIntents.candidatesFor("huawei").isNotEmpty())
    }

    @Test
    fun `known oems resolve to candidates`() {
        listOf("huawei", "honor", "oppo", "realme", "vivo", "iqoo", "samsung", "oneplus")
            .forEach { assertTrue(it, AutostartIntents.candidatesFor(it).isNotEmpty()) }
    }

    @Test
    fun `unknown manufacturer returns empty`() {
        assertTrue(AutostartIntents.candidatesFor("Pixel").isEmpty())
        assertTrue(AutostartIntents.candidatesFor("").isEmpty())
    }
}
