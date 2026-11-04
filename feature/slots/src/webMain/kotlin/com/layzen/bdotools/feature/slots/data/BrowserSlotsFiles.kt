package com.layzen.bdotools.feature.slots.data

import com.layzen.bdotools.core.folder.FolderAccess
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

    override suspend fun presetFiles(): List<String> = slotsBridge.call("presets").lines().filter { it.isNotEmpty() }

    override suspend fun readPreset(fileName: String): String = slotsBridge.call("presetRead", fileName)

    override suspend fun writePreset(fileName: String, text: String) {
        slotsBridge.call("presetWrite", fileName, text)
    }

    override suspend fun deletePreset(fileName: String) {
        slotsBridge.call("presetDelete", fileName)
    }

    override suspend fun readBackground(): ByteArray? {
        val result = slotsBridge.call("backgroundRead")
        return if (result.startsWith("1")) Base64.decode(result.substring(1)) else null
    }

    override suspend fun writeBackground(bytes: ByteArray?) {
        slotsBridge.call("backgroundWrite", bytes?.let { Base64.encode(it) }.orEmpty())
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
