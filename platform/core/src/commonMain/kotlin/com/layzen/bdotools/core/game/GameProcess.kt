package com.layzen.bdotools.core.game

/**
 * Запущена ли игра: пока она работает, она перезаписывает свои файлы и затрёт записанное тулзой.
 * `true` / `false` — на Desktop, по списку процессов; `null` — платформа не умеет это узнать (браузер).
 */
expect fun isGameRunning(): Boolean?
