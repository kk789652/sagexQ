package com.tiswu.sagex.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class KeepAlivePreferencesTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private fun newDataStore(scope: CoroutineScope): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = scope) {
            File(tmpFolder.root, "keepalive.preferences_pb")
        }

    @Test
    fun `default is disabled`() = runTest {
        val scope = CoroutineScope(Dispatchers.IO + Job())
        try {
            val prefs = KeepAlivePreferences(newDataStore(scope))
            assertFalse(prefs.isEnabled())
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `set enabled round-trips`() = runTest {
        val scope = CoroutineScope(Dispatchers.IO + Job())
        try {
            val prefs = KeepAlivePreferences(newDataStore(scope))
            prefs.setEnabled(true)
            assertTrue(prefs.isEnabled())
            prefs.setEnabled(false)
            assertFalse(prefs.isEnabled())
        } finally {
            scope.cancel()
        }
    }
}
