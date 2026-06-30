package com.example.postalon.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ImageRepository {

    suspend fun getRandomImageLink(): String? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("https://api.unsplash.com/photos/random")
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Client-ID ${Credentials.UNSPLASH_ACCESS_TOKEN}")
                connectTimeout = 10_000
                readTimeout = 10_000
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext null
            }

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            json.getJSONObject("urls").getString("regular")
        } catch (e: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }
}
