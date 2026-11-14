
package com.pes.travelexplorer.ui.details

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.pes.travelexplorer.data.model.Country
import com.pes.travelexplorer.utils.TravelIntents
import com.pes.travelexplorer.ui.components.DestinationPhoto
import com.pes.travelexplorer.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalConfiguration
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun CountryDetailScreen(
    countryCode: String,
    isSaved: Boolean = false,
    onToggleSaved: (Country) -> Unit = {},
    onPlanTrip: () -> Unit = {},
    viewModel: CountryDetailViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(),
    onBackClick: () -> Unit,
    onViewStatesClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(countryCode) {
        viewModel.loadCountryDetails(countryCode)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Country Details")
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            when (val state = uiState) {

                is CountryDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is CountryDetailUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.loadCountryDetails(countryCode)
                            }
                        ) {
                            Text("Retry")
                        }
                    }
                }

                is CountryDetailUiState.Success -> {
                    CountryDetailsContent(
                        country = state.country,
                        fallbackCountryCode = countryCode,
                        onViewStatesClick = onViewStatesClick,
                        isSaved = isSaved, onToggleSaved = onToggleSaved, onPlanTrip = onPlanTrip
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun CountryDetailsContent(
    country: Country,
    fallbackCountryCode: String,
    onViewStatesClick: (String) -> Unit,
    isSaved: Boolean = false,
    onToggleSaved: (Country) -> Unit = {},
    onPlanTrip: () -> Unit = {}
) {

    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        DestinationPhoto(country.codes?.alpha3, showCredits = true)
        Button(onClick = onPlanTrip, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.plan_trip)) }
        OutlinedButton(onClick = { onToggleSaved(country) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(if (isSaved) R.string.remove_from_wishlist else R.string.add_to_wishlist))
        }
        // Country flag
        val flagUrl = country.flag?.urlPng ?: country.flag?.urlSvg

        if (!flagUrl.isNullOrBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                GlideImage(
                    model = flagUrl,
                    contentDescription = "Flag of ${country.names?.common}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        // Country names and codes
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = country.names?.common ?: "Unknown Country",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                if (!country.names?.official.isNullOrBlank()) {
                    Text(
                        text = "Official Name: ${country.names?.official}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                if (!country.codes?.alpha3.isNullOrBlank()) {
                    Text(
                        text = "Alpha-3 Code: ${country.codes?.alpha3}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Country information
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                DetailRow(
                    label = "Capital",
                    value = country.capitals?.joinToString(", ") {
                        it.name ?: ""
                    } ?: "N/A"
                )

                DetailRow(
                    label = "Region",
                    value = country.region ?: "N/A"
                )

                DetailRow(
                    label = "Subregion",
                    value = country.subregion ?: "N/A"
                )

                val formattedPopulation = country.population?.let {
                    NumberFormat.getNumberInstance(LocalConfiguration.current.locales[0] ?: Locale.ROOT)
                        .format(it)
                } ?: "N/A"

                DetailRow(
                    label = "Population",
                    value = formattedPopulation
                )

                val currenciesText = country.currencies?.joinToString(", ") {
                    buildString {
                        append(it.name ?: "Unknown")

                        if (!it.code.isNullOrBlank()) {
                            append(" (${it.code})")
                        }

                        if (!it.symbol.isNullOrBlank()) {
                            append(" [${it.symbol}]")
                        }
                    }
                } ?: "N/A"

                DetailRow(
                    label = "Currencies",
                    value = currenciesText
                )

                val languagesText = country.languages?.joinToString(", ") {
                    it.name ?: ""
                } ?: "N/A"

                DetailRow(
                    label = "Languages",
                    value = languagesText
                )
            }
        }

        // Intent 2: Find official website using ACTION_VIEW
        OutlinedButton(
            onClick = {
                val countryName =
                    country.names?.common ?: "Unknown Country"

                val searchUrl = Uri.Builder()
                    .scheme("https")
                    .authority("www.google.com")
                    .appendPath("search")
                    .appendQueryParameter(
                        "q",
                        "$countryName travel information"
                    )
                    .build()
                    .toString()

                TravelIntents.openWebsite(
                    context,
                    searchUrl
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.research_destination))
        }

        // Use the actual Alpha-2 field; never truncate Alpha-3.
        Button(enabled = country.codes?.alpha2?.matches(Regex("[A-Za-z]{2}")) == true,
            onClick = {
                country.codes?.alpha2?.takeIf { it.matches(Regex("[A-Za-z]{2}")) }
                    ?.let { onViewStatesClick(it.uppercase(Locale.ROOT)) }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View States & Provinces")
        }
    }
}


@Composable
fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}
