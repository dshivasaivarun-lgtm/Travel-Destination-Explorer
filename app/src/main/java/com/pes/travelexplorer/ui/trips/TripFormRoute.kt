package com.pes.travelexplorer.ui.trips

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pes.travelexplorer.R
import com.pes.travelexplorer.data.local.TripEntity
import com.pes.travelexplorer.data.local.WishlistEntity
import com.pes.travelexplorer.data.repository.CountryRepository
import com.pes.travelexplorer.ui.home.detailsCode
import kotlinx.coroutines.CancellationException

@Composable
fun TripFormRoute(viewModel: TripsViewModel, tripId: Int, countryCode: String,
    saved: List<WishlistEntity>, onSaved: () -> Unit, onCancel: () -> Unit) {
    var loaded by remember(tripId) { mutableStateOf(tripId < 0) }
    var existing by remember(tripId) { mutableStateOf<TripEntity?>(null) }
    var failed by remember(tripId) { mutableStateOf(false) }
    var countries by remember { mutableStateOf(emptyList<Pair<String, String>>()) }
    var loadingCountries by remember { mutableStateOf(false) }
    var countryLoadFailed by remember { mutableStateOf(false) }
    var retry by remember { mutableIntStateOf(0) }
    val saving by viewModel.saving.collectAsState()
    val error by viewModel.error.collectAsState()
    LaunchedEffect(tripId) {
        viewModel.clearError()
        if (tripId >= 0) {
            try { existing = viewModel.readTrip(tripId); failed = existing == null }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { failed = true }
            loaded = true
        }
    }
    LaunchedEffect(retry) {
        loadingCountries = true
        val result = CountryRepository().getAllCountries()
        result.exceptionOrNull()?.let { if (it is CancellationException) throw it }
        countryLoadFailed = result.isFailure
        countries = result.getOrNull().orEmpty().mapNotNull { country ->
            country.detailsCode()?.let { it to country.names?.common.orEmpty() }
        }.filter { it.second.isNotBlank() }.sortedBy { it.second }
        loadingCountries = false
    }
    val options = (countries + saved.map { it.cca3 to it.countryName } +
        listOfNotNull(existing?.let { it.destinationCountryCode to it.destinationCountryName }))
        .distinctBy { it.first }.sortedBy { it.second }
    when {
        !loaded -> Column(Modifier.padding(24.dp)) { CircularProgressIndicator(); Text(stringResource(R.string.trip_loading)) }
        failed -> Column(Modifier.padding(24.dp)) {
            Text(stringResource(R.string.trip_not_found))
            Button(onClick = onCancel) { Text(stringResource(R.string.cancel)) }
        }
        else -> TripFormScreen(existingTrip = existing, destinations = options,
            initialCountryCode = countryCode, saving = saving, saveFailed = error != null,
            loadingDestinations = loadingCountries, destinationLoadFailed = countryLoadFailed,
            onRetryDestinations = { retry++ },
            onSave = { viewModel.saveTrip(it, onSaved) }, onCancel = onCancel)
    }
}
