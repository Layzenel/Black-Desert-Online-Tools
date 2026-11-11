package com.layzen.bdotools.feature.uieditor.data.skill

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/** [SkillDownloads] на `java.net.http`: запросы в фоновом потоке, ошибки сети — `null`. */
internal class DesktopSkillDownloads(private val client: HttpClient) : SkillDownloads {

    override suspend fun text(url: String): String? = get(url, HttpResponse.BodyHandlers.ofString())

    override suspend fun bytes(url: String): ByteArray? = get(url, HttpResponse.BodyHandlers.ofByteArray())

    private suspend fun <T> get(url: String, handler: HttpResponse.BodyHandler<T>): T? = withContext(Dispatchers.IO) {
        val request = HttpRequest.newBuilder(URI(url)).timeout(TIMEOUT).build()
        try {
            client.send(request, handler).takeIf { it.statusCode() == HTTP_OK }?.body()
        } catch (e: IOException) {
            null
        }
    }

    private companion object {
        const val HTTP_OK = 200
        val TIMEOUT: Duration = Duration.ofSeconds(30)
    }
}

internal actual fun createSkillDownloads(): SkillDownloads =
    DesktopSkillDownloads(HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build())
