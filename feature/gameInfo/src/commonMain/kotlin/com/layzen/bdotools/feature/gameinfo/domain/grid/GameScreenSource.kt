package com.layzen.bdotools.feature.gameinfo.domain.grid

/** Экран игры в единицах её интерфейса: по нему сетка поверх игры встаёт ровно по сетке редактора интерфейса. */
interface GameScreenSource {
    /**
     * Высота экрана игры в UI-px (высота разрешения, делённая на «Размер интерфейса») по файлу аккаунта игры;
     * `null` — файла нет, его не прочитать или платформа не умеет.
     */
    suspend fun uiHeight(): Double?
}
