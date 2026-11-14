
package com.pes.travelexplorer.ui.trips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.pes.travelexplorer.R
import com.pes.travelexplorer.data.local.TripEntity
import com.pes.travelexplorer.utils.TravelIntents
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsScreen(
    viewModel: TripsViewModel,
    onAddTrip: () -> Unit,
    onEditTrip: (Int) -> Unit
) {
    val trips by viewModel.trips.collectAsState()
    val context = LocalContext.current
    var deleting by remember { mutableStateOf<TripEntity?>(null) }
    deleting?.let { trip -> AlertDialog(onDismissRequest = { deleting = null },
        title = { Text(stringResource(R.string.delete_trip_confirm)) },
        text = { Text(stringResource(R.string.delete_trip_message)) },
        confirmButton = { TextButton(onClick = { viewModel.deleteTrip(trip); deleting = null }) { Text(stringResource(R.string.delete)) } },
        dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.cancel)) } }) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Trips") },
                actions = {
                    IconButton(onClick = onAddTrip) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add trip"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Button(onClick = onAddTrip) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Text("  Plan a Trip")
            }
        }
    ) { paddingValues ->

        if (trips.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_travel_empty),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "No trips planned yet",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text("Start planning your next adventure!")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = trips,
                    key = { it.id }
                ) { trip ->

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = trip.title,
                                        style = MaterialTheme.typography.titleLarge
                                    )

                                    Text(
                                        text = trip.destinationCountryName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                IconButton(
                                    onClick = { onEditTrip(trip.id) }
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit trip"
                                    )
                                }

                                IconButton(
                                    onClick = { deleting = trip }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete trip",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Start: ${trip.startDate}")
                            Text("End: ${trip.endDate}")
                            TextButton(onClick = { TravelIntents.shareText(context,
                                "${trip.title} — ${trip.destinationCountryName}\n${trip.startDate} – ${trip.endDate}\n${trip.notes}") }) {
                                Text(stringResource(R.string.share_trip))
                            }

                            if (trip.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Notes: ${trip.notes}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
