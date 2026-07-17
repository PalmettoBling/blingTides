package com.blingtides.mobile

import android.content.Context
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class WearTideSync(context: Context) {
    private val dataClient: DataClient = Wearable.getDataClient(context)

    fun sync(predictions: List<TidePrediction>) {
        val items = predictions.map { "${it.timestamp}|${it.height}" }

        val map = DataMap().apply {
            putLong("updatedAt", System.currentTimeMillis())
            putStringArrayList("predictions", ArrayList(items))
        }

        val request = PutDataMapRequest.create("/tides/latest").apply {
            dataMap.putAll(map)
        }.asPutDataRequest().setUrgent()

        dataClient.putDataItem(request)
    }
}
