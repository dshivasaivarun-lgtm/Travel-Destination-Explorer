package com.pes.travelexplorer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.pes.travelexplorer.R
import com.pes.travelexplorer.data.model.Country
import com.pes.travelexplorer.ui.home.detailsCode

/** Extracted from the original navigation screen; uses the shared v5 Country model. */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun CountryCard(country: Country, onCountryClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val code = country.detailsCode()
    val name = country.names?.common?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.explore_unknown_country)
    Card(
        onClick = { code?.let(onCountryClick) },
        enabled = code != null,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Standard Glide supports PNG; do not silently rely on an unconfigured SVG decoder.
            GlideImage(
                model = country.flag?.urlPng?.takeIf { it.isNotBlank() },
                contentDescription = stringResource(R.string.explore_flag_description, name),
                modifier = Modifier.size(64.dp, 44.dp),
                contentScale = ContentScale.Fit,
                loading = placeholder(android.R.drawable.ic_menu_gallery),
                failure = placeholder(android.R.drawable.ic_menu_gallery)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    country.region?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.explore_unknown_region),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (code == null) {
                    Text(stringResource(R.string.explore_details_unavailable), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
