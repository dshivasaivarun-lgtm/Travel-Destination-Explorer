package com.pes.travelexplorer.ui.details

import com.pes.travelexplorer.data.model.Country
import com.pes.travelexplorer.data.model.CountryCodes
import com.pes.travelexplorer.data.model.CountryNames
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CountryDetailViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun cancelsEarlierLoadSoItCannotOverwriteTheLatestResult() = runTest {
        val first = CompletableDeferred<Result<Country?>>()
        val second = CompletableDeferred<Result<Country?>>()
        var firstCancelled = false
        val viewModel = CountryDetailViewModel(loadCountry = { code ->
            if (code == "IND") {
                try { first.await() } finally { firstCancelled = true }
            } else second.await()
        })

        viewModel.loadCountryDetails("IND")
        runCurrent()
        viewModel.loadCountryDetails("JPN")
        runCurrent()
        second.complete(Result.success(country("JPN")))
        runCurrent()

        assertTrue(firstCancelled)
        assertEquals(CountryDetailUiState.Success(country("JPN")), viewModel.uiState.value)
    }

    private fun country(code: String) = Country(
        names = CountryNames(code, null), codes = CountryCodes(null, code), capitals = null,
        flag = null, region = null, subregion = null, population = null, currencies = null, languages = null
    )
}
