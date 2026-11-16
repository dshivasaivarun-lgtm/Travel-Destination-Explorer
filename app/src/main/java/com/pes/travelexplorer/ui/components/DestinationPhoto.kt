package com.pes.travelexplorer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.pes.travelexplorer.R
import com.pes.travelexplorer.data.photos.DestinationPhotoInfo
import com.pes.travelexplorer.data.repository.DestinationPhotoRepository
import com.pes.travelexplorer.utils.TravelIntents

sealed interface DestinationPhotoUiState {
    object Loading : DestinationPhotoUiState
    data class Success(val photo: DestinationPhotoInfo) : DestinationPhotoUiState
    object Unavailable : DestinationPhotoUiState
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun DestinationPhoto(
    code: String?,
    countryName: String? = null,
    showCredits: Boolean = false,
    repository: DestinationPhotoRepository = remember { DestinationPhotoRepository() }
) {
    var photoState by remember(code, countryName) {
        mutableStateOf<DestinationPhotoUiState>(DestinationPhotoUiState.Loading)
    }

    LaunchedEffect(code, countryName) {
        photoState = DestinationPhotoUiState.Loading
        val result = repository.getPhotoForCountry(code, countryName)
        result.fold(
            onSuccess = { photo ->
                if (photo != null) {
                    photoState = DestinationPhotoUiState.Success(photo)
                } else {
                    photoState = DestinationPhotoUiState.Unavailable
                }
            },
            onFailure = {
                photoState = DestinationPhotoUiState.Unavailable
            }
        )
    }

    when (val state = photoState) {
        is DestinationPhotoUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (showCredits) 210.dp else 130.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }
        is DestinationPhotoUiState.Unavailable -> {
            if (showCredits) {
                PhotoUnavailable(showCredits = true)
            }
        }
        is DestinationPhotoUiState.Success -> {
            val photo = state.photo
            val context = LocalContext.current
            Column(Modifier.fillMaxWidth()) {
                GlideImage(
                    model = photo.imageUrl,
                    contentDescription = photo.caption,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (showCredits) 210.dp else 130.dp),
                    contentScale = ContentScale.Crop,
                    loading = placeholder(android.R.drawable.ic_menu_gallery),
                    failure = placeholder { PhotoUnavailable(showCredits) }
                )
                Text(
                    text = stringResource(R.string.featured_photo, photo.caption),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
                if (showCredits) {
                    if (photo.author.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.photo_credit, photo.author, photo.license),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                    if (photo.sourceUrl.isNotBlank()) {
                        TextButton(onClick = { TravelIntents.openWebsite(context, photo.sourceUrl) }) {
                            Text(stringResource(R.string.photo_source))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoUnavailable(showCredits: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (showCredits) 210.dp else 130.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(stringResource(R.string.photo_unavailable), style = MaterialTheme.typography.bodyMedium)
    }
}
