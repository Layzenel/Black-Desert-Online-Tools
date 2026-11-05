package com.layzen.bdotools.update

import androidx.compose.runtime.Composable

/**
 * Версия приложения у правого края верхней навигации. В настольной версии рядом — кнопка «Проверить
 * обновления», а когда вышла новая версия, — тихая кнопка «Обновить до …».
 */
@Composable
internal expect fun AppVersionInfo()
