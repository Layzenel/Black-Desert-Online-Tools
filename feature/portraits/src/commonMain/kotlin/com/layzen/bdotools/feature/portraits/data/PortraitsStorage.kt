package com.layzen.bdotools.feature.portraits.data

/** Состояние доступа к папке `Документы\Black Desert`. */
sealed interface FolderAccess {
    /** Браузер не умеет записывать в папки на диске (не Chromium). */
    data object Unsupported : FolderAccess

    /** Папка ещё ни разу не выбиралась. */
    data object NotSelected : FolderAccess

    /** Папка выбрана раньше, но после перезагрузки страницы браузер снова спрашивает разрешение. */
    data class NeedsPermission(val folderName: String) : FolderAccess

    data class Granted(val folderName: String) : FolderAccess
}

enum class PortraitsError {
    Cancelled,
    NotGameFolder,
    Permission,
    Blocked,
    Network,
    NotImage,
    Unknown,
}

class PortraitsException(val error: PortraitsError, details: String? = null) :
    Exception(details ?: error.name)

/** Файлы игры в папке `Документы\Black Desert`. Все методы, кроме [access], требуют [FolderAccess.Granted]. */
interface PortraitsStorage {
    suspend fun access(): FolderAccess

    /** Открывает выбор папки. Подходит `Black Desert` или `Документы`, в которой она лежит. */
    suspend fun selectFolder(): FolderAccess.Granted

    /** Повторно запрашивает разрешение на выбранную раньше папку. Вызывать только по клику. */
    suspend fun requestPermission(): FolderAccess.Granted

    /** ID персонажей, для которых есть `FaceTexture\<ID>.bmp`. */
    suspend fun portraitIds(): List<String>

    suspend fun readPortrait(characterId: String): ByteArray

    /** Фрагменты `CharacterOrderList` из всех `UserCache\*\gamevariable.xml`. */
    suspend fun characterOrderCandidates(): List<String>

    /** Копирует текущие портреты в `FaceTexture_backup\<дата-время>`. Возвращает путь к копии. */
    suspend fun backupPortraits(characterIds: List<String>): String

    suspend fun writePortrait(characterId: String, bmp: ByteArray)

    /** Список папок `FaceTexture_backup` в формате [com.layzen.bdotools.feature.portraits.domain.parseBackupList]. */
    suspend fun backupListing(): String

    suspend fun readBackupPortrait(backup: String, characterId: String): ByteArray

    /** Удаляет папку `FaceTexture_backup\<backup>` целиком. */
    suspend fun deleteBackup(backup: String)

    /** Файлы в `Black Desert\BDO Tools\portraits`: чистые портреты и украшения. */
    suspend fun toolFileNames(): List<String>

    /** `null`, если файла нет. */
    suspend fun readToolFile(name: String): ByteArray?

    suspend fun writeToolFile(name: String, bytes: ByteArray)

    suspend fun deleteToolFile(name: String)
}

/** Источник нового изображения для портрета. */
interface ImageSource {
    /** Выбор файла на устройстве. `null`, если пользователь закрыл окно выбора. */
    suspend fun pickFromDevice(): ByteArray?

    suspend fun download(url: String): ByteArray
}
