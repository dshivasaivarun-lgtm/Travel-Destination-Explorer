package com.pes.travelexplorer.ui.home

import android.os.Build
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.pes.travelexplorer.MainActivity
import com.pes.travelexplorer.data.local.AppDatabase
import com.pes.travelexplorer.data.local.TripEntity
import com.pes.travelexplorer.data.local.WishlistEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

/** Network switching is restricted to the isolated emulator, never a physical phone. */
class OfflineSavedScreensTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun savedScreensAndTripEditWorkWithoutConnectivity() {
        assumeTrue(Build.HARDWARE in listOf("ranchu", "goldfish"))
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        fun shell(command: String): String = instrumentation.uiAutomation
            .executeShellCommand(command).use { descriptor ->
                android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor)
                    .bufferedReader().use { it.readText().trim() }
            }
        val wifi = shell("settings get global wifi_on")
        val data = shell("settings get global mobile_data")
        val db = AppDatabase.getDatabase(context)
        val code = "QZZ"
        val existingSaved = runBlocking { db.wishlistDao().getAllWishlist().first().firstOrNull { it.cca3 == code } }
        val title = "Offline acceptance ${System.nanoTime()}"
        val id = runBlocking {
            db.wishlistDao().addToWishlist(WishlistEntity(code, "Offline destination", "Offline destination", null, "Asia"))
            db.tripDao().insertTrip(TripEntity(title = title, destinationCountryCode = code,
                destinationCountryName = "Offline destination", startDate = "2026-11-16", endDate = "2026-11-16")).toInt()
        }
        var scenario: ActivityScenario<MainActivity>? = null
        try {
            shell("svc wifi disable")
            shell("svc data disable")
            scenario = ActivityScenario.launch(MainActivity::class.java)
            compose.onNode(hasText("My Wishlist") and hasClickAction()).performClick()
            compose.waitUntil(15_000) {
                compose.onAllNodesWithText(existingSaved?.countryName ?: "Offline destination").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNode(hasText("My Trips") and hasClickAction()).performClick()
            compose.waitUntil(15_000) { compose.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty() }
            // Select this test's row even if other saved trips exist.
            compose.onNodeWithText(title).performScrollTo()
            compose.onAllNodesWithContentDescription("Edit trip").filter(
                hasAnyAncestor(hasTestTag("trip_$id"))
            ).onFirst().performClick()
            compose.waitUntil(15_000) { compose.onAllNodesWithText("Update trip").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Trip title").performTextReplacement("$title updated")
            compose.onNodeWithText("Update trip").performScrollTo().performClick()
            compose.waitUntil(15_000) {
                runBlocking { db.tripDao().getTripById(id)?.title == "$title updated" }
            }
            assertEquals("2026-11-16", runBlocking { db.tripDao().getTripById(id)?.startDate })
        } finally {
            scenario?.close()
            shell("svc wifi ${if (wifi == "1") "enable" else "disable"}")
            shell("svc data ${if (data == "1") "enable" else "disable"}")
            runBlocking {
                db.tripDao().getTripById(id)?.let { db.tripDao().deleteTrip(it) }
                if (existingSaved == null) db.wishlistDao().removeFromWishlist(code)
            }
        }
    }
}
