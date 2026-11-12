package com.layzen.bdotools.feature.uieditor.data.skill

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * [SkillDownloads] с кэшем на диске: удачный ответ [inner] сохраняется в файл, а когда сети нет или ответ не 200,
 * отдаётся сохранённая копия. Так справочник и иконки доступны без сети.
 *
 * Кэш общий для аккаунтов (это данные сайта, а не файлы игрока), поэтому лежит не в папке аккаунта, а в
 * `BDO Tools\uiEditor\skillCache` — исключение из правила «всё по аккаунтам» скилла `tool-folder`.
 * Пока папка игры не выбрана, [cacheDir] даёт `null` и загрузки идут как есть, без диска.
 * Ошибки записи и чтения кэша не пробрасываются: кэш — удобство, а не часть результата.
 *
 * @property inner настоящая загрузка по сети
 * @property cacheDir папка кэша или `null`, если её пока негде создать
 */
internal class CachedSkillDownloads(
    private val inner: SkillDownloads,
    private val cacheDir: () -> Path?,
) : SkillDownloads {

    override suspend fun text(url: String): String? =
        inner.text(url)?.also { store(url, it.toByteArray()) } ?: load(url)?.let { String(it) }

    override suspend fun bytes(url: String): ByteArray? =
        inner.bytes(url)?.also { store(url, it) } ?: load(url)

    private suspend fun store(url: String, content: ByteArray) = withContext(Dispatchers.IO) {
        val file = fileFor(url) ?: return@withContext
        try {
            Files.createDirectories(file.parent)
            val temp = file.resolveSibling(file.fileName.toString() + ".tmp")
            Files.write(temp, content)
            Files.move(temp, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
        } catch (e: IOException) {
            // кэш не записался — работаем без него
        }
    }

    private suspend fun load(url: String): ByteArray? = withContext(Dispatchers.IO) {
        val file = fileFor(url)?.takeIf(Files::isRegularFile) ?: return@withContext null
        try {
            Files.readAllBytes(file)
        } catch (e: IOException) {
            null
        }
    }

    /** Имя файла из адреса без схемы: всё, кроме букв, цифр, `.`, `_` и `-`, заменяется на `_` (безопасно для Windows). */
    private fun fileFor(url: String): Path? =
        cacheDir()?.resolve(url.substringAfter("://").replace(UNSAFE, "_"))

    private companion object {
        val UNSAFE = Regex("[^A-Za-z0-9._-]")
    }
}
