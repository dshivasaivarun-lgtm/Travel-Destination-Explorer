package com.pes.travelexplorer.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.pes.travelexplorer.data.local.PreferencesManager
import com.pes.travelexplorer.data.model.Country
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pes.travelexplorer.R
import com.pes.travelexplorer.ui.components.CountryCard
import com.pes.travelexplorer.ui.components.CountrySearchBar
import com.pes.travelexplorer.ui.components.RegionFilter

@Composable
fun HomeScreen(onCountryClick: (String) -> Unit, viewModel: HomeViewModel = viewModel(),
    savedCodes: Set<String> = emptySet(), onToggleSaved: ((Country) -> Unit)? = null) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val preferences = remember { PreferencesManager(context.applicationContext) }
    val initialRegion = remember { CountryRegion.entries.firstOrNull { it.name == preferences.getRegion() } ?: CountryRegion.All }
    HomeContent(state, onCountryClick, viewModel::retry, savedCodes, onToggleSaved,
        initialRegion, { preferences.saveRegion(it.name) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(state: HomeUiState, onCountryClick: (String) -> Unit, onRetry: () -> Unit,
    savedCodes: Set<String> = emptySet(), onToggleSaved: ((Country) -> Unit)? = null,
    initialRegion: CountryRegion = CountryRegion.All, onRegionChanged: (CountryRegion) -> Unit = {}) {
    var query by rememberSaveable { mutableStateOf("") }
    var region by rememberSaveable { mutableStateOf(initialRegion) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.explore_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CountrySearchBar(query, onQueryChange = { query = it })
            RegionFilter(region, onSelect = { region = it; onRegionChanged(it) })
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (state) {
                    HomeUiState.Loading -> Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(stringResource(R.string.explore_loading))
                    }
                    HomeUiState.Error -> HomeMessage(
                        message = stringResource(R.string.explore_error),
                        action = stringResource(R.string.explore_retry),
                        onAction = onRetry
                    )
                    is HomeUiState.Content -> {
                        val filtered = remember(state.countries, query, region) {
                            filterCountries(state.countries, query, region)
                        }
                        when {
                            state.countries.isEmpty() -> HomeMessage(
                                stringResource(R.string.explore_empty_data),
                                stringResource(R.string.explore_retry), onRetry
                            )
                            filtered.isEmpty() -> HomeMessage(
                                stringResource(R.string.explore_no_matches),
                                stringResource(R.string.explore_reset_filters),
                                { query = ""; region = CountryRegion.All; onRegionChanged(region) }
                            )
                            else -> LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filtered) { country ->
                                    CountryCard(country, onCountryClick, isSaved = country.detailsCode() in savedCodes, onToggleSaved = onToggleSaved)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeMessage(message: String, action: String, onAction: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onAction) { Text(action) }
    }
}
