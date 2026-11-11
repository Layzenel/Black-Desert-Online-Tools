package com.layzen.bdotools.core.folder

import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.util.prefs.Preferences

// Узел остался с тех пор, как папку выбирали только портреты. Менять его нельзя: установленные программы
// забудут выбранную папку
private const val PREFERENCES_NODE = "com/layzen/bdotools/portraits"
private const val FOLDER_KEY = "gameFolder"
private const val ACCOUNT_KEY = "account"
private const val LANGUAGE_KEY = "language"

/**
 * Запоминает выбранную папку `Black Desert` между запусками — одну на все тулзы настольной версии,
 * как одна запись IndexedDB на сайте, — а с ней выбранный аккаунт и язык интерфейса. Хранится в [Preferences] пользователя
 * (в Windows — в реестре `HKCU\Software\JavaSoft\Prefs`).
 */
class GameFolderSettings(
    private val preferences: Preferences = Preferences.userRoot().node(PREFERENCES_NODE),
) {
    /** Сохранённая папка или `null`, если её ещё не выбирали. */
    var folder: Path?
        get() = preferences.get(FOLDER_KEY, null)?.let(::toPathOrNull)
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

    private fun toPathOrNull(text: String): Path? =
        try {
            Path.of(text)
        } catch (e: InvalidPathException) {
            null
        }
}
