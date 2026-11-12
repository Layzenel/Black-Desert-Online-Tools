package com.layzen.bdotools.feature.uieditor.data

/**
 * Прежние тексты файлов, которые переписало последнее «Применить в игру»: по ним его можно откатить.
 * Живёт только в памяти, до закрытия страницы; резервные копии на диске остаются как были.
 *
 * @property account аккаунт, чьи файлы переписаны
 * @property gameVariable `gamevariable.xml` аккаунта до записи
 * @property characterFiles файлы персонажей до первой записи после применения: путь → текст
 */
internal class ApplyJournal(val account: String, val gameVariable: String) {
    val characterFiles = mutableMapOf<String, String>()

    /** Запоминает текст файла персонажа [path] перед записью; повторная запись прежний текст не заменяет. */
    fun rememberCharacter(path: String, text: String) {
        if (path !in characterFiles) characterFiles[path] = text
    }
}
