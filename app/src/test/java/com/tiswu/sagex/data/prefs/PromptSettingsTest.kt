package com.tiswu.sagex.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PromptSettingsTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private fun newDataStore(scope: CoroutineScope): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = scope) {
            File(tmpFolder.root, "prompt.preferences_pb")
        }

    @Test
    fun `default is the summary prompt`() = runTest {
        val scope = CoroutineScope(Dispatchers.IO + Job())
        try {
            val settings = PromptSettings(newDataStore(scope))
            assertEquals(PromptSettings.DEFAULT_PROMPT, settings.currentPrompt("com.tencent.mm"))
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `set prompt round-trips per app`() = runTest {
        val scope = CoroutineScope(Dispatchers.IO + Job())
        try {
            val settings = PromptSettings(newDataStore(scope))
            settings.setPrompt("com.tencent.mm", "微信提示")

            assertEquals("微信提示", settings.currentPrompt("com.tencent.mm"))
            assertEquals(PromptSettings.DEFAULT_PROMPT, settings.currentPrompt("com.example.other"))
        } finally {
            scope.cancel()
        }
    }
}
