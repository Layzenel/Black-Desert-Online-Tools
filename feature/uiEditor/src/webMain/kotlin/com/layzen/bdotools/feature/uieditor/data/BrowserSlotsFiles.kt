package com.layzen.bdotools.feature.uieditor.data

import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.feature.uieditor.domain.error.SlotsError
import com.layzen.bdotools.feature.uieditor.domain.error.SlotsException
import com.layzen.bdotools.feature.uieditor.domain.game.GameFile
import kotlin.io.encoding.Base64

/** Доступ к файлам через File System Access API (Chrome, Edge, Opera, Яндекс.Браузер). */
internal class BrowserSlotsFiles : SlotsFiles {

    override suspend fun access(): FolderAccess = slotsBridge.access()

    override suspend fun selectFolder() = slotsBridge.selectFolder()

    override suspend fun requestPermission() = slotsBridge.requestPermission()

    override suspend fun accounts(): List<String> = slotsBridge.call("accounts").lines().filter { it.isNotEmpty() }

    override suspend fun readGameVariable(account: String): GameFile {
        val result = slotsBridge.call("read", account)
        return GameFile(text = result.substringAfter('\n'), modified = result.substringBefore('\n').toLong())
    }

    override suspend fun writeGameVariable(account: String, text: String, expectedModified: Long): String =
        slotsBridge.call("write", account, "$expectedModified\n$text")

    override suspend fun characterFiles(account: String): List<String> =
        slotsBridge.call("characters", account).lines().filter { it.isNotEmpty() }

    override suspend fun readCharacterFile(account: String, path: String): String =
        slotsBridge.call("characterRead", account, path)

    override suspend fun writeCharacterFile(account: String, path: String, text: String) {
        slotsBridge.call("characterWrite", account, "$path\n$text")
    }

    override suspend fun presetFiles(account: String): List<String> =
        slotsBridge.call("presets", account).lines().filter { it.isNotEmpty() }

    override suspend fun readPreset(account: String, fileName: String): String = slotsBridge.call("presetRead", account, fileName)

    // мост передаёт два аргумента, поэтому аккаунт и имя файла идут одной строкой «аккаунт/файл»
    override suspend fun writePreset(account: String, fileName: String, text: String) {
        slotsBridge.call("presetWrite", "$account/$fileName", text)
    }

    override suspend fun deletePreset(account: String, fileName: String) {
        slotsBridge.call("presetDelete", account, fileName)
    }

    override suspend fun readBackground(account: String): ByteArray? {
        val result = slotsBridge.call("backgroundRead", account)
        return if (result.startsWith("1")) Base64.decode(result.substring(1)) else null
    }

    override suspend fun writeBackground(account: String, bytes: ByteArray?) {
        slotsBridge.call("backgroundWrite", account, bytes?.let { Base64.encode(it) }.orEmpty())
    }

    override suspend fun adoptLooseFiles(account: String) {
        slotsBridge.call("adoptLoose", account)
    }

    override suspend fun pickImage(): ByteArray? = cancellable { Base64.decode(slotsBridge.call("pickImage")) }

    override suspend fun pickLayoutFile(): String? = cancellable { slotsBridge.call("pickLayout") }

    override suspend fun download(fileName: String, text: String) {
        slotsBridge.call("download", fileName, text)
    }

    private suspend fun <T> cancellable(action: suspend () -> T): T? =
        try {
            action()
        } catch (e: SlotsException) {
            if (e.error == SlotsError.Cancelled) null else throw e
        }
}

internal actual fun createSlotsFiles(): SlotsFiles = BrowserSlotsFiles()
