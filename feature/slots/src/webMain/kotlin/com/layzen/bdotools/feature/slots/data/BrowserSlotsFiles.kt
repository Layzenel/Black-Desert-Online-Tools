package com.layzen.bdotools.feature.slots.data

import kotlin.io.encoding.Base64

/** Доступ к файлам через File System Access API (Chrome, Edge, Opera, Яндекс.Браузер). */
internal class BrowserSlotsFiles : SlotsFiles {

    override suspend fun access(): FolderAccess {
        if (!SlotsBridge.isSupported) return FolderAccess.Unsupported
        val status = SlotsBridge.call("status")
        return when (status.substringBefore(':')) {
            "granted" -> FolderAccess.Granted(status.substringAfter(':'))
            "prompt" -> FolderAccess.NeedsPermission(status.substringAfter(':'))
            else -> FolderAccess.NotSelected
        }
    }

    override suspend fun selectFolder() = FolderAccess.Granted(SlotsBridge.call("pick"))

    override suspend fun requestPermission() = FolderAccess.Granted(SlotsBridge.call("request"))

    override suspend fun accounts(): List<String> = SlotsBridge.call("accounts").lines().filter { it.isNotEmpty() }

    override suspend fun readGameVariable(account: String): GameFile {
        val result = SlotsBridge.call("read", account)
        return GameFile(text = result.substringAfter('\n'), modified = result.substringBefore('\n').toLong())
    }

    override suspend fun writeGameVariable(account: String, text: String, expectedModified: Long): String =
        SlotsBridge.call("write", account, "$expectedModified\n$text")

    override suspend fun presetFiles(): List<String> = SlotsBridge.call("presets").lines().filter { it.isNotEmpty() }

    override suspend fun readPreset(fileName: String): String = SlotsBridge.call("presetRead", fileName)

    override suspend fun writePreset(fileName: String, text: String) {
        SlotsBridge.call("presetWrite", fileName, text)
    }

    override suspend fun deletePreset(fileName: String) {
        SlotsBridge.call("presetDelete", fileName)
    }

    override suspend fun readBackground(): ByteArray? {
        val result = SlotsBridge.call("backgroundRead")
        return if (result.startsWith("1")) Base64.decode(result.substring(1)) else null
    }

    override suspend fun writeBackground(bytes: ByteArray?) {
        SlotsBridge.call("backgroundWrite", bytes?.let { Base64.encode(it) }.orEmpty())
    }

    override suspend fun pickImage(): ByteArray? = cancellable { Base64.decode(SlotsBridge.call("pickImage")) }

    override suspend fun pickLayoutFile(): String? = cancellable { SlotsBridge.call("pickLayout") }

    override suspend fun download(fileName: String, text: String) {
        SlotsBridge.call("download", fileName, text)
    }

    private suspend fun <T> cancellable(action: suspend () -> T): T? =
        try {
            action()
        } catch (e: SlotsException) {
            if (e.error == SlotsError.Cancelled) null else throw e
        }
}

internal actual fun createSlotsFiles(): SlotsFiles = BrowserSlotsFiles()
