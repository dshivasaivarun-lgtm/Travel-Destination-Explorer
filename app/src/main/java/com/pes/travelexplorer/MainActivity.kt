package com.pes.travelexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pes.travelexplorer.ui.navigation.AppNavGraph
import com.pes.travelexplorer.ui.theme.TravelExplorerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelExplorerTheme {
                AppNavGraph()
            }
        }
    }
}
