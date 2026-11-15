package com.pes.travelexplorer.ui.cities

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pes.travelexplorer.data.model.City
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CityItemCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun validCoordinatesEnableCardAndForwardCityDetails() {
        var selected: Triple<String, Double, Double>? = null
        composeRule.setContent {
            CityItemCard(City("1", "Afra Bay", 11.675982, 92.734689, null, null)) { name, lat, lon ->
                selected = Triple(name, lat, lon)
            }
        }

        composeRule.onNodeWithText("Afra Bay").assertHasClickAction().performClick()

        assertEquals(Triple("Afra Bay", 11.675982, 92.734689), selected)
    }

    @Test
    fun missingOrInvalidCoordinatesKeepCardNonClickable() {
        composeRule.setContent {
            CityItemCard(City("missing", "No Location", null, null, null, null)) { _, _, _ -> }
        }
        composeRule.onNodeWithText("No Location").assertHasNoClickAction()

        composeRule.setContent {
            CityItemCard(City("invalid", "Invalid Location", 91.0, 0.0, null, null)) { _, _, _ -> }
        }
        composeRule.onNodeWithText("Invalid Location").assertHasNoClickAction()
    }
}
