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

class ExportSettingsTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private fun newDataStore(scope: CoroutineScope): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = scope) {
            File(tmpFolder.root, "export.preferences_pb")
        }

    @Test
    fun `default is the summarize prompt`() = runTest {
        val scope = CoroutineScope(Dispatchers.IO + Job())
        try {
            val settings = ExportSettings(newDataStore(scope))
            assertEquals(ExportSettings.DEFAULT_INSTRUCTION, settings.current())
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `set instruction round-trips`() = runTest {
        val scope = CoroutineScope(Dispatchers.IO + Job())
        try {
            val settings = ExportSettings(newDataStore(scope))
            settings.setInstructionHeader("自定义提示")
            assertEquals("自定义提示", settings.current())
        } finally {
            scope.cancel()
        }
    }
}
