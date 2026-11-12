package com.layzen.bdotools.core.folder

import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.util.prefs.Preferences
import javax.swing.filechooser.FileSystemView

private const val GAME_FOLDER_NAME = "Black Desert"

// Узел остался с тех пор, как папку выбирали только портреты. Менять его нельзя: установленные программы
// забудут выбранную папку
private const val PREFERENCES_NODE = "com/layzen/bdotools/portraits"
private const val FOLDER_KEY = "gameFolder"
private const val ACCOUNT_KEY = "account"
private const val LANGUAGE_KEY = "language"

/** «Документы» пользователя с учётом переноса в OneDrive; запасной вариант — `user.home\Documents`. */
internal fun defaultDocumentsDirectory(): Path? =
    runCatching { FileSystemView.getFileSystemView().defaultDirectory?.toPath() }.getOrNull()
        ?: System.getProperty("user.home")?.let { Path.of(it, "Documents") }

/**
 * Запоминает выбранную папку `Black Desert` между запусками — одну на все тулзы настольной версии,
 * как одна запись IndexedDB на сайте, — а с ней выбранный аккаунт и язык интерфейса. Хранится в [Preferences] пользователя
 * (в Windows — в реестре `HKCU\Software\JavaSoft\Prefs`).
 *
 * @property documents папка «Документы» пользователя, где ищется `Black Desert`, если папку ещё не выбирали
 */
class GameFolderSettings(
    private val preferences: Preferences = Preferences.userRoot().node(PREFERENCES_NODE),
    private val documents: () -> Path? = ::defaultDocumentsDirectory,
) {
    /**
     * Сохранённая папка. Если её ещё не выбирали, ищет `Документы\Black Desert` и, найдя, сохраняет как выбранную;
     * `null`, если не нашлось.
     */
    var folder: Path?
        get() = preferences.get(FOLDER_KEY, null)?.let(::toPathOrNull) ?: detectFolder()?.also { folder = it }
        set(value) {
            if (value == null) preferences.remove(FOLDER_KEY) else preferences.put(FOLDER_KEY, value.toString())
            preferences.flush()
        }

    /** Аккаунт из `UserCache`, с которым работают тулзы, или `null`, если его ещё не выбирали. */
    var account: String?
        get() = preferences.get(ACCOUNT_KEY, null)
        set(value) {
            if (value == null) preferences.remove(ACCOUNT_KEY) else preferences.put(ACCOUNT_KEY, value)
            preferences.flush()
        }

    /** Код языка интерфейса, выбранного пользователем, или `null`, если он ещё не выбирал. */
    var language: String?
        get() = preferences.get(LANGUAGE_KEY, null)
        set(value) {
            if (value == null) preferences.remove(LANGUAGE_KEY) else preferences.put(LANGUAGE_KEY, value)
            preferences.flush()
        }

    private fun detectFolder(): Path? =
        documents()?.resolve(GAME_FOLDER_NAME)?.takeIf { Files.isDirectory(it.resolve("UserCache")) || Files.isDirectory(it.resolve("FaceTexture")) }

    private fun toPathOrNull(text: String): Path? =
        try {
            Path.of(text)
        } catch (e: InvalidPathException) {
            null
        }
}
