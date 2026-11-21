package com.layzen.bdotools.composable.overlay

/**
 * Запущена ли программа от имени администратора. Игра работает с правами администратора, и только тогда Windows
 * отдаёт программе нажатия клавиш в игре (трекер комбо). На сайте — всегда `false`.
 */
expect fun isRunningAsAdmin(): Boolean
