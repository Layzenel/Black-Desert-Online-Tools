package com.layzen.bdotools.update

import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Описание последней сборки: его выкладывает `.github/workflows/desktop.yml` в релиз `desktop-latest`. */
private const val UPDATE_URL =
    "https://github.com/Layzenel/Black-Desert-Online-Tools/releases/download/desktop-latest/update.txt"
private const val BUFFER_SIZE = 64 * 1024
private val TIMEOUT: Duration = Duration.ofSeconds(20)

/** Чтение описания последней сборки и скачивание установщика с GitHub. */
class UpdateSource {

    private val client: HttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(TIMEOUT)
        .build()

    /** Последняя сборка или `null`, если файл не прочитался или не разобрался. */
    suspend fun latest(): UpdateInfo? = withContext(Dispatchers.IO) {
        val request = HttpRequest.newBuilder(URI(UPDATE_URL)).timeout(TIMEOUT).GET().build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() in 200..299) UpdateInfo.parse(response.body()) else null
    }

    /** Скачивает [url] в [target]; [onProgress] получает долю от 0 до 1, если сервер сообщил размер. */
    suspend fun download(url: String, target: Path, onProgress: (Float) -> Unit) = withContext(Dispatchers.IO) {
        val request = HttpRequest.newBuilder(URI(url)).GET().build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofInputStream())
        if (response.statusCode() !in 200..299) throw IOException("HTTP ${response.statusCode()}")
        val total = response.headers().firstValueAsLong("Content-Length").orElse(-1L)
        response.body().use { input ->
            Files.newOutputStream(target).use { output ->
                val buffer = ByteArray(BUFFER_SIZE)
                var done = 0L
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    output.write(buffer, 0, read)
                    done += read
                    if (total > 0) onProgress(done.toFloat() / total)
                }
            }
        }
    }
}
