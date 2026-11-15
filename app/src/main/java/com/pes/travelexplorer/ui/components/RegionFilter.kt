package com.pes.travelexplorer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pes.travelexplorer.R
import com.pes.travelexplorer.ui.home.CountryRegion

@Composable
fun RegionFilter(selected: CountryRegion, onSelect: (CountryRegion) -> Unit, modifier: Modifier = Modifier) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(CountryRegion.entries, key = { it.name }) { region ->
            val label = when (region) {
                CountryRegion.All -> R.string.explore_region_all
                CountryRegion.Africa -> R.string.explore_region_africa
                CountryRegion.Americas -> R.string.explore_region_americas
                CountryRegion.Asia -> R.string.explore_region_asia
                CountryRegion.Europe -> R.string.explore_region_europe
                CountryRegion.Oceania -> R.string.explore_region_oceania
            }
            FilterChip(
                selected = selected == region,
                onClick = { onSelect(region) },
                label = { Text(stringResource(label)) }
            )
        }
    }
}
