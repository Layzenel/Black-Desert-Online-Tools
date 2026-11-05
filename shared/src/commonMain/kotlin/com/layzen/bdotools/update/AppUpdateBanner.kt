package com.layzen.bdotools.update

import androidx.compose.runtime.Composable

/**
 * Плашка под верхней навигацией. На сайте — «вышла новая версия, обновите страницу». В настольной версии
 * о новой версии говорит тихая кнопка в навигации, а плашка показывает скачивание обновления и его ошибку.
 */
@Composable
internal expect fun AppUpdateBanner()
