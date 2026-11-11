package com.layzen.bdotools.feature.portraitconstructor.domain.memo

/**
 * Заметки персонажей из окна «Просмотр персонажей аккаунта» игры (`UserCache\<аккаунт>\gameVariable_lobby.xml`).
 * Пока игра запущена, она держит заметки у себя и перезаписывает файл при смене персонажа, телепорте и выходе,
 * поэтому писать стоит при закрытой игре.
 */
interface CharacterMemosRepository {

    /** Текущие заметки аккаунта: ID персонажа → заметка. */
    suspend fun memos(account: String): Map<String, String>

    /** Запущена ли игра; `null`, если платформа не умеет это узнать (сайт). */
    suspend fun isGameRunning(): Boolean?

    /**
     * Сохраняет копию файла, затем записывает в него [changes]. Файл перечитывается, чтобы не затереть свежие заметки.
     * Запущенная игра — [PortraitsError.GameRunning]: она всё равно перезаписала бы файл при смене персонажа,
     * телепорте или выходе. Возвращает путь к копии.
     */
    suspend fun write(account: String, changes: List<MemoChange>): String
}
