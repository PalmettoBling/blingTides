package com.blingtides.wear

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable

private const val TIDE_PATH = "/tides/latest"

class TideDataClient(context: Context) : DataClient.OnDataChangedListener {
    private val dataClient = Wearable.getDataClient(context)

    // mainHandler ensures the callback is always dispatched on the main thread,
    // because onDataChanged fires on a binder thread which would crash Compose state writes.
    private val mainHandler = Handler(Looper.getMainLooper())

    /** Always invoked on the main thread. */
    var onData: ((List<TidePrediction>) -> Unit)? = null

    fun startListening() {
        dataClient.addListener(this)
    }

    fun stopListening() {
        dataClient.removeListener(this)
        onData = null
    }

    /**
     * Reads the last synced tide data from the Wearable Data Layer.
     * Must be called from a background thread (uses Tasks.await internally).
     * Returns an empty list when no data has been synced yet.
     */
    fun fetchCurrent(): List<TidePrediction> {
        val buffer = Tasks.await(dataClient.dataItems)
        return try {
            buffer.mapNotNull { item ->
                if (item.uri.path != TIDE_PATH) return@mapNotNull null
                DataMapItem.fromDataItem(item).dataMap
                    .getStringArrayList("predictions")
                    ?.let { parsePredictions(it) }
            }.lastOrNull() ?: emptyList()
        } finally {
            buffer.release()
        }
    }

    /** Called on a Binder thread — dispatch the result to main before updating Compose state. */
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        val updated = mutableListOf<TidePrediction>()
        for (event in dataEvents) {
            if (event.type != DataEvent.TYPE_CHANGED) continue
            val item = event.dataItem
            if (item.uri.path != TIDE_PATH) continue
            val raw = DataMapItem.fromDataItem(item).dataMap
                .getStringArrayList("predictions") ?: continue
            updated.clear()
            updated.addAll(parsePredictions(raw))
        }
        if (updated.isNotEmpty()) {
            val snapshot = updated.toList()
            mainHandler.post { onData?.invoke(snapshot) }
        }
    }

    private fun parsePredictions(raw: List<String>): List<TidePrediction> =
        raw.mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size != 2) return@mapNotNull null
            val height = parts[1].toDoubleOrNull() ?: return@mapNotNull null
            TidePrediction(timestamp = parts[0], height = height)
        }
}
