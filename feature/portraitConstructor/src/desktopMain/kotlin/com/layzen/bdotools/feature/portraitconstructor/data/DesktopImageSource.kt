package com.layzen.bdotools.feature.portraitconstructor.data

import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlin.io.path.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val connectTimeout = Duration.ofSeconds(15)
private const val USER_AGENT = "BDO Tools"
private const val IMAGE_TYPE_PREFIX = "image/"
private val successCodes = 200..299

/**
 * Картинка с диска через системное окно выбора или по ссылке через `java.net.http`.
 * Ошибки — как у загрузки в браузере: нет ответа или код не 2xx — [PortraitsError.Network],
 * ответ не картинка — [PortraitsError.NotImage]. Запрета скачивания с других сайтов, как в браузере, здесь нет.
 */
internal class DesktopImageSource(
    private val dialogs: SystemDialogs,
    private val client: HttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(connectTimeout)
        .build(),
) : ImageSource {

    override suspend fun pickFromDevice(): ByteArray? {
        val file = dialogs.pickImage() ?: return null
        return fileCall { file.readBytes() }
    }

    override suspend fun download(url: String): ByteArray = withContext(Dispatchers.IO) {
        val response = try {
            client.send(request(url), HttpResponse.BodyHandlers.ofByteArray())
        } catch (e: IOException) {
            throw PortraitsException(PortraitsError.Network, e.message)
        }
        if (response.statusCode() !in successCodes) throw PortraitsException(PortraitsError.Network)
        val type = response.headers().firstValue("Content-Type").orElse("")
        if (!type.startsWith(IMAGE_TYPE_PREFIX, ignoreCase = true)) throw PortraitsException(PortraitsError.NotImage)
        response.body()
    }

    private fun request(url: String): HttpRequest =
        try {
            HttpRequest.newBuilder(URI(url.trim())).header("User-Agent", USER_AGENT).GET().build()
        } catch (e: URISyntaxException) {
            throw PortraitsException(PortraitsError.Network, e.message)
        } catch (e: IllegalArgumentException) {
            throw PortraitsException(PortraitsError.Network, e.message)
        }
}
