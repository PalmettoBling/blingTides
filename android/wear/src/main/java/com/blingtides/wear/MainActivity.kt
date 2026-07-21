package com.blingtides.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class TideType { HIGH, LOW }

data class TideExtreme(
    val prediction: TidePrediction,
    val type: TideType
)

/**
 * Scans for local maxima (high) and minima (low) in a 6-minute-interval prediction series.
 * Tide data is very smooth so immediate-neighbor comparison is reliable.
 */
fun findExtremes(predictions: List<TidePrediction>): List<TideExtreme> {
    if (predictions.size < 3) return emptyList()
    return buildList {
        for (i in 1 until predictions.size - 1) {
            val prev = predictions[i - 1].height
            val curr = predictions[i].height
            val next = predictions[i + 1].height
            when {
                curr > prev && curr > next -> add(TideExtreme(predictions[i], TideType.HIGH))
                curr < prev && curr < next -> add(TideExtreme(predictions[i], TideType.LOW))
            }
        }
    }
}

/** Formats "2025-07-21 14:06" → "2:06 PM" */
fun formatTime(timestamp: String): String {
    return try {
        val timePart = timestamp.substringAfter(" ")
        val (hour, minute) = timePart.split(":").map { it.toInt() }
        val suffix = if (hour < 12) "AM" else "PM"
        val hour12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        "$hour12:${minute.toString().padStart(2, '0')} $suffix"
    } catch (_: Exception) {
        timestamp
    }
}

fun formatHeight(value: Double): String = String.format("%.2f ft", value)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val predictions = remember { mutableStateOf(emptyList<TidePrediction>()) }
            val dataClient = remember { TideDataClient(this) }

            LaunchedEffect(Unit) {
                // onData is always called on main thread by TideDataClient
                dataClient.onData = { predictions.value = it }
                dataClient.startListening()
                // Load any previously synced data immediately
                val initial = withContext(Dispatchers.IO) { dataClient.fetchCurrent() }
                if (initial.isNotEmpty()) predictions.value = initial
            }

            DisposableEffect(Unit) {
                onDispose { dataClient.stopListening() }
            }

            MaterialTheme {
                WearTideScreen(predictions = predictions.value)
            }
        }
    }
}

@Composable
fun WearTideScreen(predictions: List<TidePrediction>) {
    if (predictions.isEmpty()) {
        EmptyState()
        return
    }

    val extremes = remember(predictions) { findExtremes(predictions) }
    val nextHigh = extremes.firstOrNull { it.type == TideType.HIGH }
    val nextLow = extremes.firstOrNull { it.type == TideType.LOW }
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Tides",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
            )
        }

        if (nextHigh != null) {
            item {
                TideExtremeCard(
                    label = "Next High",
                    extreme = nextHigh,
                    cardColor = Color(0xFF1B4F72)
                )
            }
        }

        if (nextLow != null) {
            item {
                TideExtremeCard(
                    label = "Next Low",
                    extreme = nextLow,
                    cardColor = Color(0xFF1A5276)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(1.dp)
                    .background(Color.Gray)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Show up to 48 entries — one per 6-minute interval is a lot;
        // skip every other point to show a reading every ~12 minutes.
        val displayed = predictions.filterIndexed { index, _ -> index % 2 == 0 }.take(48)
        items(displayed) { item ->
            TideRow(item = item, predictions = predictions)
        }
    }
}

@Composable
fun TideExtremeCard(label: String, extreme: TideExtreme, cardColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.88f)
            .background(cardColor, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xFFADB5BD)
            )
            Text(
                text = formatTime(extreme.prediction.timestamp),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = formatHeight(extreme.prediction.height),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (extreme.type == TideType.HIGH) Color(0xFF5DADE2) else Color(0xFF76D7C4)
        )
    }
}

@Composable
fun TideRow(item: TidePrediction, predictions: List<TidePrediction>) {
    val idx = predictions.indexOf(item)
    val trend = when {
        idx > 0 && item.height > predictions[idx - 1].height -> "▲"
        idx > 0 && item.height < predictions[idx - 1].height -> "▼"
        else -> "─"
    }
    val trendColor = when (trend) {
        "▲" -> Color(0xFF5DADE2)
        "▼" -> Color(0xFF76D7C4)
        else -> Color.Gray
    }

    Row(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatTime(item.timestamp),
            fontSize = 11.sp,
            color = Color.LightGray
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = trend,
                fontSize = 10.sp,
                color = trendColor,
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(
                text = formatHeight(item.height),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No tide data",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Open phone app\n& tap Sync Watch",
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}
