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
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ConversationReadStoreTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private fun newDataStore(scope: CoroutineScope): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = scope) {
            File(tmpFolder.root, "read.preferences_pb")
        }

    @Test
    fun `unset conversation has no read position`() = runTest {
        val scope = CoroutineScope(Dispatchers.IO + Job())
        try {
            val store = ConversationReadStore(newDataStore(scope))
            assertNull(store.lastReadId("conv-a"))
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `read position round-trips per conversation`() = runTest {
        val scope = CoroutineScope(Dispatchers.IO + Job())
        try {
            val store = ConversationReadStore(newDataStore(scope))
            store.setLastReadId("conv-a", 42L)

            assertEquals(42L, store.lastReadId("conv-a"))
            assertNull(store.lastReadId("conv-b"))
        } finally {
            scope.cancel()
        }
    }
}
