package com.blingtides.mobile

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WearTideSync(context: Context) {
    private val dataClient = Wearable.getDataClient(context)

    /**
     * Serialises [predictions] and writes them to the Wearable Data Layer.
     * Marked suspend so callers stay on the main thread; the actual network
     * call is switched to an IO thread internally.
     */
    suspend fun sync(predictions: List<TidePrediction>) = withContext(Dispatchers.IO) {
        val items = predictions.map { "${it.timestamp}|${it.height}" }

        val map = DataMap().apply {
            putLong("updatedAt", System.currentTimeMillis())
            putStringArrayList("predictions", ArrayList(items))
        }

        val request = PutDataMapRequest.create("/tides/latest").apply {
            dataMap.putAll(map)
        }.asPutDataRequest().setUrgent()

        Tasks.await(dataClient.putDataItem(request))
    }
}
