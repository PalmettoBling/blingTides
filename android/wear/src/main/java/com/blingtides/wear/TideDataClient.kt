package com.blingtides.wear

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable

class TideDataClient(context: Context) : DataClient.OnDataChangedListener {
    private val dataClient = Wearable.getDataClient(context)

    var onData: ((List<TidePrediction>) -> Unit)? = null

    fun startListening() {
        dataClient.addListener(this)
    }

    fun stopListening() {
        dataClient.removeListener(this)
    }

    fun fetchCurrent() {
        val items = Tasks.await(dataClient.dataItems)
        val tides = mutableListOf<TidePrediction>()

        for (item in items) {
            if (item.uri.path != "/tides/latest") {
                continue
            }

            val map = DataMapItem.fromDataItem(item).dataMap
            val predictions = map.getStringArrayList("predictions") ?: continue
            tides.clear()
            tides.addAll(parsePredictions(predictions))
        }

        onData?.invoke(tides)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            val item = event.dataItem
            if (item.uri.path != "/tides/latest") {
                continue
            }

            val map = DataMapItem.fromDataItem(item).dataMap
            val predictions = map.getStringArrayList("predictions") ?: continue
            onData?.invoke(parsePredictions(predictions))
        }
    }

    private fun parsePredictions(raw: List<String>): List<TidePrediction> {
        return raw.mapNotNull {
            val split = it.split("|")
            if (split.size != 2) {
                return@mapNotNull null
            }

            val value = split[1].toDoubleOrNull() ?: return@mapNotNull null
            TidePrediction(timestamp = split[0], height = value)
        }
    }
}
