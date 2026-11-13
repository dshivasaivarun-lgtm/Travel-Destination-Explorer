package com.pes.travelexplorer.ui.home

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.pes.travelexplorer.MainActivity
import com.pes.travelexplorer.BuildConfig
import com.pes.travelexplorer.data.local.AppDatabase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

/** Runs against the actual navigation graph and on-device Room database. */
class CoreJourneyTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun wishlistDetailsPrefilledTripAndCancelStayConnected() {
        assumeTrue(BuildConfig.API_KEY.isNotBlank())
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = AppDatabase.getDatabase(context)
        val originallySaved = runBlocking { db.wishlistDao().isInWishlist("IND") }
        try {
            compose.onNodeWithText("Search by country name").performTextInput("iNdIa")
            compose.onNode(hasText("All") and SemanticsMatcher.keyIsDefined(androidx.compose.ui.semantics.SemanticsProperties.Selected)).performScrollTo().performClick()
            compose.waitUntil(90_000) { compose.onAllNodesWithText("India").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("India").performClick()
            compose.waitUntil(90_000) { compose.onAllNodesWithText("Plan trip", substring=false).fetchSemanticsNodes().isNotEmpty() }
            if (!originallySaved) {
                compose.onNodeWithText("Add to wishlist").performScrollTo().performClick()
                compose.waitUntil(10_000) { runBlocking { db.wishlistDao().isInWishlist("IND") } }
            }
            compose.onNodeWithText("My Wishlist").performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithText("India").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("India").performClick()
            compose.onNodeWithText("Plan trip").performScrollTo().performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithText("India").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Trip title").performTextInput("Integration journey")
            compose.onNodeWithText("Save trip").performScrollTo().performClick()
            compose.onNodeWithText("Enter a title, select a destination and use valid dates with end on or after start.").assertIsDisplayed()
            compose.onNodeWithText("Cancel").performScrollTo().performClick()
            assertTrue(runBlocking { db.tripDao().getAllTrips().first().none { it.title == "Integration journey" } })
        } finally {
            if (!originallySaved) runBlocking { db.wishlistDao().removeFromWishlist("IND") }
        }
    }
}
