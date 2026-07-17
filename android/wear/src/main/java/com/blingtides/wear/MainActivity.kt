package com.blingtides.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val predictionsState = remember { mutableStateOf(emptyList<TidePrediction>()) }
            val dataClient = remember { TideDataClient(this) }

            LaunchedEffect(Unit) {
                dataClient.onData = { predictions ->
                    predictionsState.value = predictions
                }

                dataClient.startListening()
                withContext(Dispatchers.IO) {
                    dataClient.fetchCurrent()
                }
            }

            DisposableEffect(Unit) {
                onDispose {
                    dataClient.stopListening()
                }
            }

            WearTideScreen(predictions = predictionsState.value)
        }
    }
}

private fun formatHeight(value: Double): String = String.format("%.2f ft", value)

@androidx.compose.runtime.Composable
private fun WearTideScreen(predictions: List<TidePrediction>) {
    if (predictions.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No tide data yet")
            Text("Open phone app and tap Sync Watch")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(predictions.take(24)) { item ->
            Column {
                Text(item.timestamp)
                Text(formatHeight(item.height))
            }
        }
    }
}
