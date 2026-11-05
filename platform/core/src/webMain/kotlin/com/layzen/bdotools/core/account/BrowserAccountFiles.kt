package com.layzen.bdotools.core.account

import com.layzen.bdotools.core.folder.FolderBridge
import kotlin.io.encoding.Base64

private const val RECORD_FIELDS = 3

/** Аккаунты через File System Access API: папку выбирают фичи, мост читает её из общей записи IndexedDB. */
internal class BrowserAccountFiles : AccountFiles {

    private val bridge = FolderBridge(
        name = "__bdoAccounts",
        requiredFolder = "UserCache",
        script = ACCOUNTS_BRIDGE_SOURCE,
        failure = { code -> IllegalStateException(code) },
    )

    override suspend fun accountFiles(): List<AccountFile> =
        bridge.call("accounts")
            .split(ACCOUNT_SEPARATOR)
            .map { it.split('\n', limit = RECORD_FIELDS) }
            .filter { it.size == RECORD_FIELDS }
            .map { (id, modified, text) -> AccountFile(id, modified.toDoubleOrNull()?.toLong() ?: 0L, text) }

    override suspend fun readPortrait(characterId: String): ByteArray? =
        bridge.call("face", "$characterId.bmp").takeIf { it.isNotEmpty() }?.let { Base64.decode(it) }
}

internal actual fun createAccountFiles(): AccountFiles = BrowserAccountFiles()
