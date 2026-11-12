package com.layzen.bdotools.feature.uieditor.presentation.ui

import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_folder_select
import org.jetbrains.compose.resources.getString

/**
 * Загружает строки редактора текущего языка заранее, до первого показа страницы: в браузере файл строк скачивается
 * при первом обращении, и без этого кнопки на миг остаются пустыми.
 */
suspend fun preloadSlotsStrings() {
    getString(Res.string.slots_folder_select)
}
