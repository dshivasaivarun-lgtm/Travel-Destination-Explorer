package com.pes.travelexplorer.ui.home

import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.pes.travelexplorer.BuildConfig
import com.pes.travelexplorer.MainActivity
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Live integration test: requires a privately installed, working API_KEY and internet. */
class ExploreNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun liveCountrySearchRegionDetailsAndBack() {
        assumeTrue("A local development API key is required", BuildConfig.API_KEY.isNotBlank())
        compose.onNodeWithText("Search by country name").performTextInput("iNdIa")
        compose.waitUntil(timeoutMillis = 90_000) {
            compose.onAllNodesWithText("India").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("India").assertIsDisplayed()
        compose.onNode(hasText("Europe") and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected))
            .performScrollTo().performClick()
        compose.onNodeWithText("No countries match your search and region.").assertIsDisplayed()
        compose.onNode(hasText("Asia") and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected))
            .performScrollTo().performClick()
        compose.onNodeWithText("India").assertIsDisplayed()
        saveScreenshot("explore-live-filtered.png")
        compose.onNodeWithText("India").performClick()
        compose.onNodeWithText("Country Details").assertIsDisplayed()
        compose.waitUntil(timeoutMillis = 90_000) {
            compose.onAllNodesWithText("Alpha-3 Code: IND").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Alpha-3 Code: IND").assertIsDisplayed()
        saveScreenshot("details-live-ind.png")
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithText("Search by country name").assertTextContains("iNdIa")
        compose.onNode(hasText("Asia") and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected)).assertIsSelected()
        compose.onNodeWithContentDescription("Clear search").performClick()
        compose.onNode(hasText("All") and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected))
            .performScrollTo().performClick()
        saveScreenshot("explore-live-list.png")
    }

    private fun saveScreenshot(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        File(context.filesDir, name).outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap()
                .compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
