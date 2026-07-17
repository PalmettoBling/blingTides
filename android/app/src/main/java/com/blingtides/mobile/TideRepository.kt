package com.blingtides.mobile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TideRepository {
    suspend fun fetchPredictions(
        station: String = "8665530",
        rangeHours: Int = 48,
        date: LocalDate = LocalDate.now()
    ): List<TidePrediction> = withContext(Dispatchers.IO) {
        val beginDate = date.format(DateTimeFormatter.BASIC_ISO_DATE)
        val endpoint = URL(
            "https://api.tidesandcurrents.noaa.gov/api/prod/datagetter" +
                "?begin_date=$beginDate" +
                "&range=$rangeHours" +
                "&station=$station" +
                "&product=predictions" +
                "&datum=STND" +
                "&time_zone=lst_ldt" +
                "&units=english" +
                "&format=json"
        )

        val connection = endpoint.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 12_000
        connection.readTimeout = 12_000

        try {
            if (connection.responseCode !in 200..299) {
                return@withContext emptyList()
            }

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(body)
            val predictions = root.optJSONArray("predictions") ?: return@withContext emptyList()

            buildList {
                for (index in 0 until predictions.length()) {
                    val item = predictions.optJSONObject(index) ?: continue
                    val timestamp = item.optString("t")
                    val value = item.optString("v").toDoubleOrNull() ?: continue
                    add(TidePrediction(timestamp = timestamp, height = value))
                }
            }
        } finally {
            connection.disconnect()
        }
    }
}
