package com.layzen.bdotools.feature.portraitconstructor.presentation.ui

import com.layzen.bdotools.feature.portraitconstructor.resources.Res
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_folder_select
import org.jetbrains.compose.resources.getString

/**
 * Загружает строки тулзы текущего языка заранее, до первого показа страницы: в браузере файл строк скачивается
 * при первом обращении, и без этого кнопки на миг остаются пустыми.
 */
suspend fun preloadPortraitsStrings() {
    getString(Res.string.portraits_folder_select)
}
