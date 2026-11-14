package com.pes.travelexplorer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.pes.travelexplorer.R
import com.pes.travelexplorer.data.photos.DestinationPhotos
import com.pes.travelexplorer.utils.TravelIntents

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun DestinationPhoto(code: String?, showCredits: Boolean = false) {
    val photo = DestinationPhotos.find(code)
    if (photo == null) {
        if (showCredits) Text(stringResource(R.string.photo_unavailable), style = MaterialTheme.typography.bodySmall)
        return
    }
    val context = LocalContext.current
    Column(Modifier.fillMaxWidth()) {
        GlideImage(model = photo.imageUrl, contentDescription = photo.caption,
            modifier = Modifier.fillMaxWidth().height(if (showCredits) 210.dp else 130.dp),
            contentScale = ContentScale.Crop,
            loading = placeholder(android.R.drawable.ic_menu_gallery),
            failure = placeholder(android.R.drawable.ic_menu_gallery))
        Text(stringResource(R.string.featured_photo, photo.caption), style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
        if (showCredits) {
            Text(stringResource(R.string.photo_credit, photo.author, photo.license), style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { TravelIntents.openWebsite(context, photo.sourceUrl) }) {
                Text(stringResource(R.string.photo_source))
            }
        }
    }
}
