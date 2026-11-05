package com.layzen.bdotools.core.account

/**
 * Файлы в выбранной папке `Black Desert`, по которым узнаются аккаунты. Реализация своя у каждой платформы,
 * создаёт её [createAccountFiles]. Папку выбирают фичи; пока доступа нет, методы бросают исключение.
 */
interface AccountFiles {
    /** `gamevariable.xml` из каждой подпапки `UserCache`. */
    suspend fun accountFiles(): List<AccountFile>

    /** `FaceTexture\<characterId>.bmp` или `null`, если файла нет. */
    suspend fun readPortrait(characterId: String): ByteArray?
}

/** Реализация [AccountFiles] текущей платформы. */
internal expect fun createAccountFiles(): AccountFiles
