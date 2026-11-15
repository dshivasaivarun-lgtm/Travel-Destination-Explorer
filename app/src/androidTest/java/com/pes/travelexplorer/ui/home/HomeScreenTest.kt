package com.pes.travelexplorer.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.pes.travelexplorer.data.model.Country
import com.pes.travelexplorer.data.model.CountryCodes
import com.pes.travelexplorer.data.model.CountryNames
import com.pes.travelexplorer.ui.theme.TravelExplorerTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Fixtures verify Explore in isolation; these tests do not assert live API success. */
class HomeScreenTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val countries = listOf(
        country("India", "Asia", "IND"), country("Japan", "Asia", "JPN"),
        country("France", "Europe", "FRA")
    )

    @Test fun searchRegionEmptyResetAndAlpha3Selection() {
        var selected: String? = null
        compose.setContent {
            TravelExplorerTheme {
                HomeContent(HomeUiState.Content(countries), { selected = it }, {})
            }
        }
        compose.onNodeWithText("Search by country name").performTextInput("iNd")
        compose.onNodeWithText("India").assertIsDisplayed()
        compose.onNodeWithText("Japan").assertDoesNotExist()
        compose.onNodeWithText("Europe").performScrollTo().performClick()
        compose.onNodeWithText("No countries match your search and region.").assertIsDisplayed()
        compose.onNodeWithText("Reset filters").performClick()
        compose.onNodeWithText("India").performClick()
        compose.runOnIdle { assertEquals("IND", selected) }
        compose.onNodeWithText("Search by country name").performTextInput("asia")
        compose.onNodeWithText("No countries match your search and region.").assertIsDisplayed()
        compose.onNodeWithContentDescription("Clear search").performClick()
        compose.onNodeWithText("India").assertIsDisplayed()
    }

    @Test fun loadingFailureRetryAndEmptyData() {
        val state = mutableStateOf<HomeUiState>(HomeUiState.Loading)
        var retries = 0
        compose.setContent {
            TravelExplorerTheme { HomeContent(state.value, {}, { retries++ }) }
        }
        compose.onNodeWithText("Loading countries…").assertIsDisplayed()
        compose.runOnIdle { state.value = HomeUiState.Error }
        compose.onNodeWithText("Retry").performClick()
        compose.runOnIdle {
            assertEquals(1, retries)
            state.value = HomeUiState.Content(emptyList())
        }
        compose.onNodeWithText("No countries are available. Try loading again.").assertIsDisplayed()
        compose.onNodeWithText("Retry").performClick()
        compose.runOnIdle { assertEquals(2, retries) }
    }

    @Test fun filtersSurviveSavedStateRestoration() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            TravelExplorerTheme { HomeContent(HomeUiState.Content(countries), {}, {}) }
        }
        compose.onNodeWithText("Search by country name").performTextInput("ind")
        compose.onNode(hasText("Asia") and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected)).performScrollTo().performClick()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Search by country name").assertTextContains("ind")
        compose.onNode(hasText("Asia") and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected)).assertIsSelected()
        compose.onNodeWithText("India").assertIsDisplayed()
    }

    @Test fun missingOptionalFieldsAndInvalidIdentityAreSafe() {
        var selected: String? = null
        compose.setContent {
            TravelExplorerTheme {
                HomeContent(HomeUiState.Content(listOf(country(null, null, null))), { selected = it }, {})
            }
        }
        compose.onNodeWithText("Unknown country").assertIsDisplayed()
        compose.onNodeWithText("Region unavailable").assertIsDisplayed()
        compose.onNodeWithText("Details unavailable").assertIsDisplayed()
        compose.onNodeWithText("Unknown country").assertIsNotEnabled()
        compose.runOnIdle { assertEquals(null, selected) }
    }

    @Test fun viewModelRetryLoadsContentWithoutReloadingOnFilterChanges() {
        var attempts = 0
        lateinit var viewModel: HomeViewModel
        compose.runOnUiThread {
            viewModel = HomeViewModel {
                attempts++
                if (attempts == 1) Result.failure(IllegalStateException("test failure"))
                else Result.success(countries)
            }
        }
        compose.setContent {
            TravelExplorerTheme { HomeScreen({}, viewModel) }
        }
        compose.onNodeWithText("Retry").performClick()
        compose.onNodeWithText("India").assertIsDisplayed()
        compose.onNodeWithText("Search by country name").performTextInput("ind")
        compose.runOnIdle { assertEquals(2, attempts) }
    }

    private fun country(name: String?, region: String?, code: String?) = Country(
        names = CountryNames(name, null), codes = CountryCodes("XX", code),
        capitals = null, flag = null, region = region, subregion = null,
        population = null, currencies = null, languages = null
    )
}
